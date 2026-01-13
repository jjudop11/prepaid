package com.prepaid.common.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락 이름 (SpEL 지원)
     */
    String key();

    /**
     * 시간 단위
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 락 획득 대기 시간
     */
    long waitTime() default 5L;

    /**
     * 락 임대 시간 (이 시간이 지나면 락이 자동으로 해제됨)
     */
    long leaseTime() default 3L;
}
