package com.ll.quizzle.global.redis.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * 락에 사용될 키 (SpEL 표현식 지원)
     */
    String key();
    
    /**
     * 락이 자동으로 해제되는 시간 (밀리초)
     */
    long leaseTime() default 5000;
    
    /**
     * 락 획득 대기 시간 (밀리초)
     */
    long waitTime() default 3000;
} 