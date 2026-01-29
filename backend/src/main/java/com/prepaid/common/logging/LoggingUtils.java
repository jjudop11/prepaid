package com.prepaid.common.logging;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * MDC (Mapped Diagnostic Context) 관리 유틸리티
 * 
 * 구조화된 로깅을 위한 컨텍스트 정보 관리
 */
public class LoggingUtils {
    
    // MDC 키 상수
    public static final String USER_ID = "userId";
    public static final String TX_TYPE = "txType";
    public static final String AMOUNT = "amount";
    public static final String ORDER_ID = "orderId";
    public static final String PAYMENT_KEY = "paymentKey";
    public static final String MERCHANT_UID = "merchantUid";
    
    /**
     * 사용자 컨텍스트 설정
     */
    public static void setUserContext(Long userId) {
        if (userId != null) {
            MDC.put(USER_ID, String.valueOf(userId));
        }
    }
    
    /**
     * 트랜잭션 컨텍스트 설정
     */
    public static void setTransactionContext(String txType, Long amount) {
        if (txType != null) {
            MDC.put(TX_TYPE, txType);
        }
        if (amount != null) {
            MDC.put(AMOUNT, String.valueOf(amount));
        }
    }
    
    /**
     * 주문 컨텍스트 설정
     */
    public static void setOrderContext(String orderId) {
        if (orderId != null) {
            MDC.put(ORDER_ID, orderId);
        }
    }
    
    /**
     * 결제 컨텍스트 설정
     */
    public static void setPaymentContext(String paymentKey) {
        if (paymentKey != null) {
            MDC.put(PAYMENT_KEY, paymentKey);
        }
    }
    
    /**
     * 가맹점 컨텍스트 설정
     */
    public static void setMerchantContext(String merchantUid) {
        if (merchantUid != null) {
            MDC.put(MERCHANT_UID, merchantUid);
        }
    }
    
    /**
     * 전체 컨텍스트 설정 (원장 거래용)
     */
    public static void setLedgerContext(Long userId, String txType, Long amount, String referenceId) {
        setUserContext(userId);
        setTransactionContext(txType, amount);
        if (referenceId != null) {
            MDC.put(ORDER_ID, referenceId);
        }
    }
    
    /**
     * MDC 컨텍스트 초기화
     */
    public static void clearContext() {
        MDC.clear();
    }
    
    /**
     * 특정 키만 제거
     */
    public static void remove(String key) {
        MDC.remove(key);
    }
    
    /**
     * 현재 MDC 컨텍스트를 Runnable에 전파
     * 비동기 작업 시 사용
     */
    public static Runnable withContext(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            }
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
    
    /**
     * 현재 MDC 컨텍스트를 Callable에 전파
     * 비동기 작업 시 사용
     */
    public static <T> Callable<T> withContext(Callable<T> callable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            }
            try {
                return callable.call();
            } finally {
                MDC.clear();
            }
        };
    }
    
    /**
     * Try-with-resources를 위한 AutoCloseable 래퍼
     * 
     * 사용 예:
     * try (var ctx = LoggingUtils.context(userId, "CHARGE", 10000L)) {
     *     // 로직 실행
     * } // 자동으로 컨텍스트 정리
     */
    public static AutoCloseable context(Long userId, String txType, Long amount) {
        setLedgerContext(userId, txType, amount, null);
        return LoggingUtils::clearContext;
    }
}
