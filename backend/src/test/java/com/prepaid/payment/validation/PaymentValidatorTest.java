package com.prepaid.payment.validation;

import com.prepaid.common.exception.ErrorCode;
import com.prepaid.common.exception.specific.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * PaymentValidator 단위 테스트
 */
@DisplayName("PaymentValidator 테스트")
class PaymentValidatorTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private PaymentValidator paymentValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentValidator = new PaymentValidator(redisTemplate);
        
        // 설정값 주입
        ReflectionTestUtils.setField(paymentValidator, "minChargeAmount", 1000L);
        ReflectionTestUtils.setField(paymentValidator, "maxChargeAmount", 1000000L);
        ReflectionTestUtils.setField(paymentValidator, "dailyChargeLimit", 5000000L);
        ReflectionTestUtils.setField(paymentValidator, "minUseAmount", 100L);
        ReflectionTestUtils.setField(paymentValidator, "maxUseAmount", 1000000L);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("충전 금액 검증 - 정상")
    void validateChargeAmount_Success() {
        // given
        Long userId = 1L;
        Long amount = 10000L;
        when(valueOperations.get(anyString())).thenReturn(null);

        // when & then
        assertDoesNotThrow(() -> paymentValidator.validateChargeAmount(userId, amount));
    }

    @Test
    @DisplayName("충전 금액 검증 - 최소 금액 미만")
    void validateChargeAmount_BelowMinimum() {
        // given
        Long userId = 1L;
        Long amount = 500L;

        // when & then
        assertThatThrownBy(() -> paymentValidator.validateChargeAmount(userId, amount))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessageContaining("최소 충전 금액");
    }

    @Test
    @DisplayName("충전 금액 검증 - 최대 금액 초과")
    void validateChargeAmount_ExceedsMaximum() {
        // given
        Long userId = 1L;
        Long amount = 2000000L;

        // when & then
        assertThatThrownBy(() -> paymentValidator.validateChargeAmount(userId, amount))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessageContaining("최대 충전 금액");
    }

    @Test
    @DisplayName("충전 금액 검증 - 일일 한도 초과")
    void validateChargeAmount_ExceedsDailyLimit() {
        // given
        Long userId = 1L;
        Long amount = 1000000L;
        when(valueOperations.get(anyString())).thenReturn("4500000"); // 이미 450만원 충전됨

        // when & then
        assertThatThrownBy(() -> paymentValidator.validateChargeAmount(userId, amount))
                .isInstanceOf(InvalidAmountException.class)
                .hasMessageContaining("일일 충전 한도");
    }

    @Test
    @DisplayName("사용 금액 검증 - 정상")
    void validateUseAmount_Success() {
        // given
        Long amount = 10000L;

        // when & then
        assertDoesNotThrow(() -> paymentValidator.validateUseAmount(amount));
    }

    @Test
    @DisplayName("사용 금액 검증 - 음수")
    void validateUseAmount_Negative() {
        // given
        Long amount = -1000L;

        // when & then
        assertThatThrownBy(() -> paymentValidator.validateUseAmount(amount))
                .isInstanceOf(InvalidAmountException.class);
    }
}
