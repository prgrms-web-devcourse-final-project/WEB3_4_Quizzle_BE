package com.ll.quizzle.global.redis.lock;

import java.util.concurrent.TimeUnit;

/**
 * 테스트 코드에서 RedisLockService 를 Mocking 하기 위해 인터페이스 분리 (그 외 확장성(ex.zookeeper)과 유연성 고려)
 */
public interface DistributedLockService {
    /**
     * 분산 락 획득
     */
    boolean acquireLock(String key, String value, long waitTime, long leaseTime, TimeUnit timeUnit);
    /**
     * 분산 락 해제
     */
    boolean releaseLock(String key, String value);
} 