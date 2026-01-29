package com.prepaid.payment.repository;

import com.prepaid.payment.domain.Payment;
import com.prepaid.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 결제 Repository
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * 주문 ID로 결제 조회
     */
    Optional<Payment> findByOrderId(String orderId);
    
    /**
     * 사용자별 결제 상태 조회
     */
    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);
    
    /**
     * 결제 키로 조회
     */
    Optional<Payment> findByPaymentKey(String paymentKey);
}
