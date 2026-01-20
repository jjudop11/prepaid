package com.prepaid.payment.validation;

import com.prepaid.common.exception.ErrorCode;
import com.prepaid.common.exception.specific.InvalidAmountException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * 금액 검증 서비스
 * - 최소/최대 금액 검증
 * - 일일 한도 확인
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentValidator {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${payment.charge.min-amount}")
    private Long minChargeAmount;

    @Value("${payment.charge.max-amount}")
    private Long maxChargeAmount;

    @Value("${payment.charge.daily-limit}")
    private Long dailyChargeLimit;

    @Value("${payment.use.min-amount}")
    private Long minUseAmount;

    @Value("${payment.use.max-amount}")
    private Long maxUseAmount;

    /**
     * 충전 금액 검증
     */
    public void validateChargeAmount(Long userId, Long amount) {
        // 1. 음수 및 0 체크
        if (amount == null || amount <= 0) {
            throw new InvalidAmountException(ErrorCode.INVALID_AMOUNT, "충전 금액은 0보다 커야 합니다.");
        }

        // 2. 최소 금액 체크
        if (amount < minChargeAmount) {
            throw new InvalidAmountException(ErrorCode.BELOW_MINIMUM_CHARGE,
                    String.format("최소 충전 금액은 %,d원입니다.", minChargeAmount));
        }

        // 3. 최대 금액 체크
        if (amount > maxChargeAmount) {
            throw new InvalidAmountException(ErrorCode.EXCEEDS_MAXIMUM_CHARGE,
                    String.format("최대 충전 금액은 %,d원입니다.", maxChargeAmount));
        }

        // 4. 일일 한도 체크
        checkDailyLimit(userId, amount);
    }

    /**
     * 사용 금액 검증
     */
    public void validateUseAmount(Long amount) {
        // 1. 음수 및 0 체크
        if (amount == null || amount <= 0) {
            throw new InvalidAmountException(ErrorCode.INVALID_AMOUNT, "사용 금액은 0보다 커야 합니다.");
        }

        // 2. 최소 금액 체크
        if (amount < minUseAmount) {
            throw new InvalidAmountException(ErrorCode.INVALID_AMOUNT,
                    String.format("최소 사용 금액은 %,d원입니다.", minUseAmount));
        }

        // 3. 최대 금액 체크
        if (amount > maxUseAmount) {
            throw new InvalidAmountException(ErrorCode.INVALID_AMOUNT,
                    String.format("최대 사용 금액은 %,d원입니다.", maxUseAmount));
        }
    }

    /**
     * 일일 충전 한도 확인
     * Redis를 사용하여 오늘 충전한 총 금액 추적
     */
    private void checkDailyLimit(Long userId, Long amount) {
        String key = getDailyLimitKey(userId);
        
        // 오늘 충전한 총 금액 조회
        String currentTotalStr = redisTemplate.opsForValue().get(key);
        Long currentTotal = currentTotalStr != null ? Long.parseLong(currentTotalStr) : 0L;
        
        // 이번 충전 포함 총액
        Long newTotal = currentTotal + amount;
        
        if (newTotal > dailyChargeLimit) {
            throw new InvalidAmountException(ErrorCode.EXCEEDS_DAILY_LIMIT,
                    String.format("일일 충전 한도(%,d원)를 초과합니다. 현재 충전액: %,d원",
                            dailyChargeLimit, currentTotal));
        }
        
        log.info("일일 한도 체크: userId={}, 현재 총액={}원, 요청 금액={}원, 한도={}원",
                userId, currentTotal, amount, dailyChargeLimit);
    }

    /**
     * 충전 성공 시 일일 한도 업데이트
     */
    public void updateDailyLimit(Long userId, Long amount) {
        String key = getDailyLimitKey(userId);
        
        // 현재 총액 조회
        String currentTotalStr = redisTemplate.opsForValue().get(key);
        Long currentTotal = currentTotalStr != null ? Long.parseLong(currentTotalStr) : 0L;
        
        // 새로운 총액 저장 (자정까지 유효)
        Long newTotal = currentTotal + amount;
        redisTemplate.opsForValue().set(key, newTotal.toString(), getDurationUntilMidnight());
        
        log.info("일일 한도 업데이트: userId={}, 새로운 총액={}원", userId, newTotal);
    }

    /**
     * 일일 한도 키 생성
     */
    private String getDailyLimitKey(Long userId) {
        String today = LocalDate.now().toString();
        return String.format("daily:charge:%s:%d", today, userId);
    }

    /**
     * 자정까지 남은 시간 계산
     */
    private Duration getDurationUntilMidnight() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        long secondsUntilMidnight = Duration.between(
                java.time.LocalDateTime.now(),
                tomorrow.atStartOfDay()
        ).getSeconds();
        return Duration.ofSeconds(secondsUntilMidnight);
    }
}
