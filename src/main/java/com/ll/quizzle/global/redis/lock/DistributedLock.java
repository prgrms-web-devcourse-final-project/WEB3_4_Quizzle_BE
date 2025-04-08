package com.ll.quizzle.global.redis.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * SpEL 표현식으로 동적 키를 생성하는 값
     */
    String key();
    
    /**
     * 락의 유효 시간
     */
    long leaseTime() default 5000;
    
    /**
     * 락 획득 시도 시간
     */
    long waitTime() default 3000;
} 