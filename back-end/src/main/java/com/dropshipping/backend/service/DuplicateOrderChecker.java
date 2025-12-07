package com.dropshipping.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Duplicate order detection within a 24-hour window using Redis.
 * Key: customerId:productId:supplierId
 */
@Component
@RequiredArgsConstructor
public class DuplicateOrderChecker {

    private final StringRedisTemplate redisTemplate;

    private static final Duration WINDOW = Duration.ofHours(24);

    public boolean isDuplicateAndLock(Long customerId, Long productId, Long supplierId) {
        if (customerId == null || productId == null || supplierId == null) {
            return false;
        }
        String key = buildKey(customerId, productId, supplierId);
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", WINDOW);
        // setIfAbsent returns true if successfully set (i.e., not duplicate)
        // return true if it's a duplicate (already present)
        return Boolean.FALSE.equals(success);
    }

    public void clearLock(Long customerId, Long productId, Long supplierId) {
        String key = buildKey(customerId, productId, supplierId);
        redisTemplate.delete(key);
    }

    private String buildKey(Long customerId, Long productId, Long supplierId) {
        return "dup:" + customerId + ":" + productId + ":" + supplierId;
    }
}
