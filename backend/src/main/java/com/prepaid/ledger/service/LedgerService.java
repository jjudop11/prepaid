package com.prepaid.ledger.service;

import com.prepaid.common.exception.ErrorCode;
import com.prepaid.common.exception.specific.InsufficientBalanceException;
import com.prepaid.common.exception.specific.WalletNotFoundException;
import com.prepaid.common.lock.DistributedLock;
import com.prepaid.domain.User;
import com.prepaid.domain.Wallet;
import com.prepaid.event.domain.ChargeCompletedEvent;
import com.prepaid.event.domain.SpendCompletedEvent;
import com.prepaid.event.service.EventPublisher;
import com.prepaid.ledger.domain.*;
import com.prepaid.ledger.repository.*;
import com.prepaid.payment.validation.PaymentValidator;
import com.prepaid.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

        private final WalletRepository walletRepository;
        private final LedgerEntryRepository ledgerEntryRepository;
        private final LedgerLineRepository ledgerLineRepository;
        private final ChargeLotRepository chargeLotRepository;
        private final SpendAllocationRepository spendAllocationRepository;
        private final EventPublisher eventPublisher;
        private final PaymentValidator paymentValidator;

        @Transactional
        public void recordCharge(User user, Long amount, String paymentKey, String orderId) {
                // 1. 지갑 조회 또는 생성
                Wallet wallet = walletRepository.findByUserId(user.getId())
                                .orElseGet(() -> {
                                        Wallet newWallet = Wallet.builder().user(user).build();
                                        return walletRepository.save(newWallet);
                                });

                // 2. 원장 엔트리 생성 (헤더)
                LedgerEntry entry = LedgerEntry.builder()
                                .wallet(wallet)
                                .txType(TxType.CHARGE)
                                .status(LedgerStatus.POSTED)
                                .referenceId(orderId)
                                .idempotencyKey(paymentKey)
                                .bucketType(BucketType.PAID)
                                .memo("Charge via Toss")
                                .build();
                ledgerEntryRepository.save(entry);

                // 3. 원장 라인 생성 (복식부기)
                // 차변(Debit): 지갑 현금 자산 증가 (+amount)
                LedgerLine debit = LedgerLine.builder()
                                .entry(entry)
                                .accountCode(AccountCode.WALLET_CASH)
                                .amountSigned(amount)
                                .build();

                // 대변(Credit): 외부 입금 (부채/출처) (-amount)
                LedgerLine credit = LedgerLine.builder()
                                .entry(entry)
                                .accountCode(AccountCode.EXTERNAL_CASH_IN)
                                .amountSigned(-amount)
                                .build();

                ledgerLineRepository.save(debit);
                ledgerLineRepository.save(credit);

                // 4. Charge Lot 생성 (유료 잔액에 대한 FIFO 추적)
                ChargeLot chargeLot = ChargeLot.builder()
                                .wallet(wallet)
                                .bucketType(BucketType.PAID)
                                .originalEntryId(entry.getId())
                                .amountTotal(amount)
                                .amountRemaining(amount)
                                .build();
                chargeLotRepository.save(chargeLot);

                // 5. 파생된 잔액(Derived Balance) 업데이트
                wallet.addBalance(amount, BucketType.PAID);

                // 6. 충전 완료 이벤트 발행 (트랜잭션 커밋 후)
                publishChargeCompletedEvent(user.getId(), amount, wallet.getBalance(), paymentKey, orderId);
        }

        @DistributedLock(key = "'wallet:' + #user.id")
        public void useBalance(User user, Long amount, String merchantUid) {
                // 1. 금액 검증
                paymentValidator.validateUseAmount(amount);
                
                // 2. 지갑 잔액 확인
                Wallet wallet = walletRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new WalletNotFoundException());

        if (wallet.getBalance() < amount) {
                throw new InsufficientBalanceException("현재 잔액: " + wallet.getBalance() + "원, 요청 금액: " + amount + "원");
                }

                // 2. 사용(USE)을 위한 원장 엔트리 생성
                String idempotencyKey = UUID.randomUUID().toString(); // 필요 시 상위에서 전달받음
                LedgerEntry useEntry = LedgerEntry.builder()
                                .wallet(wallet)
                                .txType(TxType.USE)
                                .status(LedgerStatus.POSTED)
                                .referenceId(merchantUid)
                                .idempotencyKey(idempotencyKey)
                                .memo("Use Balance")
                                .build();
                ledgerEntryRepository.save(useEntry);

                // 3. FIFO 차감 로직 (우선순위: 무료 -> 유료)
                Long remainingToUse = amount;

                // 3-1. 무료(FREE) 버킷 차감
                remainingToUse = consumeBucket(wallet, BucketType.FREE, remainingToUse, useEntry);

                // 3-2. 유료(PAID) 버킷 차감 (필요한 경우)
                if (remainingToUse > 0) {
                        remainingToUse = consumeBucket(wallet, BucketType.PAID, remainingToUse, useEntry);
                }

                if (remainingToUse > 0) {
                // 잔액 확인을 통과했으므로 발생하면 안 되는 상황.
                // 동시성 문제가 없다면(DistributedLock 사용 중) 안전함.
                throw new InsufficientBalanceException("예기치 않은 오류: 차감 중 잔액 불일치 발생 (남은 금액: " + remainingToUse + "원)");
                }

                // 4. 원장 라인 생성
                // 차변(Debit): 지갑 현금 감소 (-amount) (부채 감소)
                LedgerLine debit = LedgerLine.builder()
                                .entry(useEntry)
                                .accountCode(AccountCode.WALLET_CASH)
                                .amountSigned(-amount)
                                .build();

                // 대변(Credit): 가맹점 지급 대기 / 서비스 수익 (+amount)
                LedgerLine credit = LedgerLine.builder()
                                .entry(useEntry)
                                .accountCode(AccountCode.EXTERNAL_CASH_IN) // 임시로 외부 입금 계정 재사용 (또는 별도 계정)
                                .amountSigned(amount)
                                .build();

                ledgerLineRepository.save(debit);
                ledgerLineRepository.save(credit);

                // 5. 지갑 잔액 업데이트
                // consumeBucket에서 Lot은 차감되지만 Wallet 엔티티의 총 잔액 필드는 여기서 업데이트하거나
                // consumeBucket 내부에서 처리 가능. 여기서는 단순화하여 내부 로직에 위임하거나 직접 차감.
                // consumeBucket에서 처리하는 것이 일관성이 좋음.

                // 6. 사용 완료 이벤트 발행 (트랜잭션 커밋 후)
                publishSpendCompletedEvent(user.getId(), -amount, wallet.getBalance(), merchantUid);
        }

        // 특정 버킷 타입에서 포인트 차감하는 헬퍼 메서드
        private Long consumeBucket(Wallet wallet, BucketType bucketType, Long amountToConsume, LedgerEntry useEntry) {
                if (amountToConsume <= 0)
                        return 0L;

                List<ChargeLot> lots = chargeLotRepository
                                .findAllByWalletIdAndBucketTypeAndAmountRemainingGreaterThanOrderByCreatedAtAsc(
                                                wallet.getId(), bucketType, 0L);

                Long remaining = amountToConsume;
                Long consumedInThisBucket = 0L;

                for (ChargeLot lot : lots) {
                        if (remaining <= 0)
                                break;

                        Long consumeFromLot = Math.min(lot.getAmountRemaining(), remaining);

                        // Lot 업데이트
                        lot.decreaseRemaining(consumeFromLot);

                        SpendAllocation allocation = SpendAllocation.builder()
                                        .spendEntry(useEntry)
                                        .chargeLot(lot)
                                        .amountConsumed(consumeFromLot)
                                        .build();
                        spendAllocationRepository.save(allocation);

                        remaining -= consumeFromLot;
                        consumedInThisBucket += consumeFromLot;
                }

                // 해당 버킷의 지갑 잔액 업데이트
                if (consumedInThisBucket > 0) {
                        // addBalance를 음수로 호출하여 차감 (subtractBalance 대신)
                        wallet.addBalance(-consumedInThisBucket, bucketType);
                }

                return remaining;
        }

        /**
         * 충전 완료 이벤트 발행 (트랜잭션 커밋 후 실행)
         */
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void publishChargeCompletedEvent(Long userId, Long amount, Long newBalance, String paymentKey,
                        String orderId) {
                ChargeCompletedEvent event = new ChargeCompletedEvent(userId, amount, newBalance, paymentKey, orderId);
                eventPublisher.publish(event);
        }

        /**
         * 사용 완료 이벤트 발행 (트랜잭션 커밋 후 실행)
         */
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void publishSpendCompletedEvent(Long userId, Long amount, Long newBalance, String referenceId) {
                SpendCompletedEvent event = new SpendCompletedEvent(userId, amount, newBalance, referenceId, "잔액 사용");
                eventPublisher.publish(event);
        }
}
