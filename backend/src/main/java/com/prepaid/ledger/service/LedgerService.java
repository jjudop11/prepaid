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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.prepaid.common.logging.LoggingUtils.*;

@Slf4j
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
                // MDC에 컨텍스트 정보 추가 (구조화된 로깅)
                setLedgerContext(user.getId(), "CHARGE", amount, orderId);
                setPaymentContext(paymentKey);
                
                try {
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
                        
                        log.info("충전 완료");
                } finally {
                        // MDC 정리
                        clearContext();
                }
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
         * 충전 취소 (특정 ChargeLot 무효화)
         * @param chargeEntryId 취소할 충전 거래의 LedgerEntry ID
         * @param user 사용자
         * @param reason 취소 사유
         * @return 취소된 금액
         */
        @Transactional
        public Long cancelCharge(Long chargeEntryId, User user, String reason) {
                setUserContext(user.getId());
                setTransactionContext("CHARGE_CANCEL", null);
                
                try {
                // 1. 원본 충전 거래 조회
                LedgerEntry chargeEntry = ledgerEntryRepository.findById(chargeEntryId)
                        .orElseThrow(() -> new IllegalArgumentException("충전 거래를 찾을 수 없습니다"));
                
                // 2. 권한 검증 (본인 거래인지)
                if (!chargeEntry.getWallet().getUser().getId().equals(user.getId())) {
                        throw new com.prepaid.common.exception.specific.UnauthorizedException("본인의 거래만 취소할 수 있습니다");
                }
                
                // 3. 충전 거래인지 확인
                if (chargeEntry.getTxType() != TxType.CHARGE) {
                        throw new IllegalStateException("충전 거래가 아닙니다");
                }
                
                // 4. 해당 ChargeLot 조회
                ChargeLot lot = chargeLotRepository.findByOriginalEntryId(chargeEntryId)
                        .orElseThrow(() -> new IllegalStateException("ChargeLot을 찾을 수 없습니다"));
                
                // 5. 취소 가능 여부 확인
                if (lot.getAmountRemaining() == 0) {
                        throw new IllegalStateException("이미 모두 사용된 충전 건입니다. 취소 불가능합니다.");
                }
                
                Long cancelableAmount = lot.getAmountRemaining();
                Long usedAmount = lot.getAmountTotal() - lot.getAmountRemaining();
                
                // 6. 부분 사용된 경우 경고 로그
                if (usedAmount > 0) {
                        log.warn("부분 사용된 충전 건 취소: userId={}, chargeId={}, total={}, used={}, canceling={}", 
                                user.getId(), chargeEntryId, lot.getAmountTotal(), usedAmount, cancelableAmount);
                }
                
                // 7. CHARGE_CANCEL 원장 엔트리 생성
                LedgerEntry cancelEntry = LedgerEntry.builder()
                        .wallet(chargeEntry.getWallet())
                        .txType(TxType.CHARGE_CANCEL)
                        .status(LedgerStatus.POSTED)
                        .referenceId(chargeEntry.getReferenceId())
                        .idempotencyKey(UUID.randomUUID().toString())
                        .memo("Charge Cancel: " + reason)
                        .build();
                ledgerEntryRepository.save(cancelEntry);
                
                // 8. 원장 라인 생성 (복식부기 - CHARGE의 역방향)
                LedgerLine debit = LedgerLine.builder()
                        .entry(cancelEntry)
                        .accountCode(AccountCode.EXTERNAL_CASH_IN)
                        .amountSigned(cancelableAmount)  // 외부로 돌려줌
                        .build();
                
                LedgerLine credit = LedgerLine.builder()
                        .entry(cancelEntry)
                        .accountCode(AccountCode.WALLET_CASH)
                        .amountSigned(-cancelableAmount)  // 지갑에서 차감
                        .build();
                
                ledgerLineRepository.save(debit);
                ledgerLineRepository.save(credit);
                
                // 9. ChargeLot 무효화 (remaining = 0으로 만듦)
                lot.decreaseRemaining(cancelableAmount);
                
                // 10. Wallet 잔액 차감
                Wallet wallet = chargeEntry.getWallet();
                wallet.addBalance(-cancelableAmount, BucketType.PAID);
                
                log.info("충전 취소 완료: chargeId={}, canceledAmount={}", chargeEntryId, cancelableAmount);
                
                return cancelableAmount;
                } finally {
                        clearContext();
                }
        }

        /**
         * 결제 취소 (사용 거래 원복)
         * @param useEntryId 취소할 사용 거래의 LedgerEntry ID
         * @param user 사용자
         * @param reason 취소 사유
         * @return 복구된 금액
         */
        @Transactional
        public Long reverseUse(Long useEntryId, User user, String reason) {
                // 1. 원본 사용 거래 조회
                LedgerEntry useEntry = ledgerEntryRepository.findById(useEntryId)
                        .orElseThrow(() -> new IllegalArgumentException("사용 거래를 찾을 수 없습니다"));
                
                // 2. 권한 검증
                if (!useEntry.getWallet().getUser().getId().equals(user.getId())) {
                        throw new com.prepaid.common.exception.specific.UnauthorizedException("본인의 거래만 취소할 수 있습니다");
                }
                
                // 3. 사용 거래인지 확인
                if (useEntry.getTxType() != TxType.USE) {
                        throw new IllegalStateException("사용 거래가 아닙니다");
                }
                
                // 4. 이미 취소된 거래인지 확인
                boolean alreadyReversed = ledgerEntryRepository.existsByTxTypeAndReferenceId(
                        TxType.REVERSAL, useEntry.getReferenceId());
                if (alreadyReversed) {
                        throw new IllegalStateException("이미 취소된 거래입니다");
                }
                
                // 5. SpendAllocation 조회 (어떤 ChargeLot에서 얼마나 사용했는지)
                List<SpendAllocation> allocations = spendAllocationRepository
                        .findAllBySpendEntryId(useEntryId);
                
                if (allocations.isEmpty()) {
                        throw new IllegalStateException("SpendAllocation을 찾을 수 없습니다");
                }
                
                // 6. REVERSAL 원장 엔트리 생성
                LedgerEntry reversalEntry = LedgerEntry.builder()
                        .wallet(useEntry.getWallet())
                        .txType(TxType.REVERSAL)
                        .status(LedgerStatus.POSTED)
                        .referenceId(useEntry.getReferenceId())
                        .idempotencyKey(UUID.randomUUID().toString())
                        .memo("Use Reversal: " + reason)
                        .build();
                ledgerEntryRepository.save(reversalEntry);
                
                // 7. 각 ChargeLot에 금액 복구
                Long totalReversed = 0L;
                for (SpendAllocation allocation : allocations) {
                        ChargeLot lot = allocation.getChargeLot();
                        Long amountToRestore = allocation.getAmountConsumed();
                        
                        // ChargeLot 잔액 복구
                        lot.increaseRemaining(amountToRestore);
                        
                        totalReversed += amountToRestore;
                        
                        log.debug("ChargeLot 복구: lotId={}, restored={}", lot.getId(), amountToRestore);
                }
                
                // 8. 원장 라인 생성 (복식부기 - USE의 역방향)
                LedgerLine debit = LedgerLine.builder()
                        .entry(reversalEntry)
                        .accountCode(AccountCode.WALLET_CASH)
                        .amountSigned(totalReversed)  // 지갑에 복구
                        .build();
                
                LedgerLine credit = LedgerLine.builder()
                        .entry(reversalEntry)
                        .accountCode(AccountCode.EXTERNAL_CASH_IN)
                        .amountSigned(-totalReversed)  // 외부에서 회수
                        .build();
                
                ledgerLineRepository.save(debit);
                ledgerLineRepository.save(credit);
                
                // 9. Wallet 잔액 복구
                Wallet wallet = useEntry.getWallet();
                // allocations에서 버킷 타입 복구 (PAID/FREE 구분)
                for (SpendAllocation allocation : allocations) {
                        BucketType bucketType = allocation.getChargeLot().getBucketType();
                        wallet.addBalance(allocation.getAmountConsumed(), bucketType);
                }
                
                log.info("사용 취소 완료: userId={}, useId={}, reversedAmount={}", 
                        user.getId(), useEntryId, totalReversed);
                
                return totalReversed;
        }

        /**
         * 충전 완료 이벤트 발행 (트랜잭션 커밋 후 실행)
         */
        public void publishChargeCompletedEvent(Long userId, Long amount, Long newBalance, String paymentKey,
                        String orderId) {
                ChargeCompletedEvent event = new ChargeCompletedEvent(userId, amount, newBalance, paymentKey, orderId);
                eventPublisher.publish(event);
        }

        /**
         * 사용 완료 이벤트 발행 (트랜잭션 커밋 후 실행)
         */
        public void publishSpendCompletedEvent(Long userId, Long amount, Long newBalance, String referenceId) {
                SpendCompletedEvent event = new SpendCompletedEvent(userId, amount, newBalance, referenceId, "잔액 사용");
                eventPublisher.publish(event);
        }

        /**
         * 환불/인출 (순수한 잔액 인출)
         * - 은행 계좌로 출금하는 경우
         * - FIFO로 ChargeLot 차감
         * - 충전 취소가 아닌 순수한 인출 용도
         * 
         * @param user 사용자
         * @param amount 인출 금액
         * @param reason 인출 사유
         */
        @Transactional
        public void recordWithdrawal(User user, Long amount, String reason) {
                // 1. 지갑 조회
                Wallet wallet = walletRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new WalletNotFoundException());

                // 2. 잔액이 충분한지 확인 (인출할 금액만큼 있어야 함)
                if (wallet.getBalance() < amount) {
                        throw new InsufficientBalanceException("인출 금액보다 잔액이 부족합니다. 현재 잔액: " + wallet.getBalance() + "원");
                }

                // 3. 원장 엔트리 생성 (REFUND 타입 - 인출 용도)
                String referenceId = "WITHDRAWAL_" + System.currentTimeMillis();
                String idempotencyKey = java.util.UUID.randomUUID().toString();
                LedgerEntry withdrawalEntry = LedgerEntry.builder()
                                .wallet(wallet)
                                .txType(TxType.REFUND)
                                .status(LedgerStatus.POSTED)
                                .referenceId(referenceId)
                                .idempotencyKey(idempotencyKey)
                                .memo("Withdrawal: " + reason)
                                .build();
                ledgerEntryRepository.save(withdrawalEntry);

                // 4. 원장 라인 생성 (복식부기)
                // 차변: 외부 출금
                LedgerLine debit = LedgerLine.builder()
                                .entry(withdrawalEntry)
                                .accountCode(AccountCode.EXTERNAL_CASH_IN)
                                .amountSigned(amount)
                                .build();

                // 대변: 지갑 현금 감소
                LedgerLine credit = LedgerLine.builder()
                                .entry(withdrawalEntry)
                                .accountCode(AccountCode.WALLET_CASH)
                                .amountSigned(-amount)
                                .build();

                ledgerLineRepository.save(debit);
                ledgerLineRepository.save(credit);

                // 5. ChargeLot에서 차감 (FIFO, PAID 버킷만)
                Long remainingToDeduct = amount;
                List<ChargeLot> lots = chargeLotRepository
                                .findAllByWalletIdAndBucketTypeAndAmountRemainingGreaterThanOrderByCreatedAtAsc(
                                                wallet.getId(), BucketType.PAID, 0L);

                for (ChargeLot lot : lots) {
                        if (remainingToDeduct <= 0) break;

                        Long deductFromLot = Math.min(lot.getAmountRemaining(), remainingToDeduct);
                        lot.decreaseRemaining(deductFromLot);
                        remainingToDeduct -= deductFromLot;
                }

                // 6. 지갑 잔액 차감
                wallet.addBalance(-amount, BucketType.PAID);

                log.info("잔액 인출 완료: userId={}, amount={}, reason={}", user.getId(), amount, reason);
        }
        
        /**
         * 환불 기록 (하위 호환성 유지)
         * @deprecated Use recordWithdrawal instead for pure withdrawal, or cancelCharge for charge cancellation
         */
        @Deprecated
        @Transactional
        public void recordRefund(User user, Long amount, String orderId, String reason) {
                log.warn("recordRefund is deprecated. Use recordWithdrawal for pure withdrawal or cancelCharge for charge cancellation.");
                recordWithdrawal(user, amount, reason);
        }
}
