package org.example.url_shorter_test_task_gorokhov.limit;

import lombok.extern.slf4j.Slf4j;
import org.example.url_shorter_test_task_gorokhov.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Service
@Slf4j
public class UserRateLimiterService implements RateLimiter {

    private final int maxPerUser;

    private final Map<String, Semaphore> semaphores = new ConcurrentHashMap<>();

    public UserRateLimiterService(@Value("${app.rateLimiter.maxPerUser:100}") int maxPerUser) {
        this.maxPerUser = maxPerUser;
    }

    @Override
    public String tryAcquire(String userId) {
        log.info("Trying to acquire by user {}", userId);
        Semaphore semaphore = semaphores.computeIfAbsent(userId, id -> new Semaphore(maxPerUser));
        boolean acquired = semaphore.tryAcquire();
        if (!acquired) {
            log.error("The limit of simultaneous requests (100) has been exceeded for the user: {}", userId);
            throw new TooManyRequestsException("The limit of simultaneous requests (100) has been exceeded for the user: " + userId);
        }
        int permitsLeft = semaphore.availablePermits();
        log.info("Permit successfully captured by {}. Available permits: {}",
                userId, permitsLeft);
        return null; // permitId не нужен для локальной реализации
    }

    @Override
    public void release(String userId, String permitId) {
        log.info("Trying to release permit for user {}.", userId);
        Semaphore semaphore = semaphores.get(userId);
        if (semaphore == null) return;
        semaphore.release();
        log.info("Permit is released for user {}.", userId);
        if (semaphore.availablePermits() == maxPerUser) {
            log.info("All permits are free: deleting semaphore for {}", userId);
            semaphores.remove(userId, semaphore);
        }
    }

}