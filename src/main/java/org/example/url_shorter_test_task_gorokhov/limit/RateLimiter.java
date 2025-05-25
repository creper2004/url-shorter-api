package org.example.url_shorter_test_task_gorokhov.limit;


public interface RateLimiter {

    /**
     * Попытка получить permit для userId.
     * @param userId пользователь
     * @return permitId (или null — если реализация не требует)
     */
    String tryAcquire(String userId);

    /**
     * Освобождение permit-а для userId.
     * @param userId пользователь
     * @param permitId ID permit-а (или null — если реализация не требует)
     */
    void release(String userId, String permitId);
}
