package org.example.url_shorter_test_task_gorokhov.limit;

import lombok.extern.slf4j.Slf4j;
import org.example.url_shorter_test_task_gorokhov.exception.TooManyRequestsException;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DistributedUserRateLimiterService implements RateLimiter {

    private final int maxPerUser;

    private final RedissonClient redissonClient;
    private final long permitTtlSeconds;

    public DistributedUserRateLimiterService(RedissonClient redissonClient,
                                             @Value("${app.rateLimiter.permitTtlSeconds:120}") long permitTtlSeconds,
                                             @Value("${app.rateLimiter.maxPerUser:100}") int maxPerUser) {
        this.redissonClient = redissonClient;
        this.permitTtlSeconds = permitTtlSeconds;
        this.maxPerUser = maxPerUser;
    }

    @Override
    public String tryAcquire(String userId) {
        log.info("Trying to acquire by user {}", userId);
        String semaphoreKey = "shorten:sem:" + userId;
        RPermitExpirableSemaphore semaphore = redissonClient.getPermitExpirableSemaphore(semaphoreKey);
        semaphore.trySetPermits(maxPerUser);

        try {
            List<String> permitIds = semaphore.tryAcquire(1, 0, permitTtlSeconds, TimeUnit.SECONDS);
            if (permitIds == null || permitIds.isEmpty()) {
                log.error("The limit of simultaneous requests (100) has been exceeded for the user: {}", userId);
                throw new TooManyRequestsException(
                        "The limit of simultaneous requests (100) has been exceeded for the user: " + userId
                );
            }
            log.info("Permit successfully captured by {}.", userId);
            return permitIds.get(0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while acquiring permit");
            throw new TooManyRequestsException("Interrupted while acquiring permit");
        }
    }

    @Override
    public void release(String userId, String permitId) {
        log.info("Trying to release permit for user {}.", userId);
        if (permitId == null) return;
        String semaphoreKey = "shorten:sem:" + userId;
        RPermitExpirableSemaphore semaphore = redissonClient.getPermitExpirableSemaphore(semaphoreKey);

        try {
            semaphore.release(permitId);
            log.info("Permit is released for user {}.", userId);
        } catch (IllegalArgumentException ex) {
            log.warn("Permit {} for user {} already expired or released", permitId, userId);
        }

        int available = semaphore.availablePermits();
        if (available == maxPerUser) {
            redissonClient.getKeys().delete(semaphoreKey);
            log.info("All permits are free: deleting semaphore for {}", userId);
        }
    }
}
