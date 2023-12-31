package com.zhangzhankui.samples.common.security.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 登录尝试限制器
 * <p>
 * 基于 Redis 实现分布式限流
 */
@Slf4j
@Component
public class LoginAttemptLimiter {

    private final StringRedisTemplate redisTemplate;

    public LoginAttemptLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 最大尝试次数
     */
    private static final int MAX_ATTEMPTS = 5;

    /**
     * 锁定时间（分钟）
     */
    private static final int LOCK_TIME_MINUTES = 15;

    /**
     * Redis Key 前缀
     */
    private static final String KEY_PREFIX = "login:attempt:";

    /**
     * 检查用户是否被锁定
     *
     * @param username 用户名
     * @return true-已锁定，false-未锁定
     */
    public boolean isBlocked(String username) {
        String key = KEY_PREFIX + username;
        String value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return false;
        }
        
        try {
            return Integer.parseInt(value) >= MAX_ATTEMPTS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 获取剩余锁定时间（秒）
     *
     * @param username 用户名
     * @return 剩余锁定秒数，0表示未锁定
     */
    public long getRemainingLockTime(String username) {
        String key = KEY_PREFIX + username;
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null && expire > 0 ? expire : 0;
    }

    /**
     * 获取剩余尝试次数
     *
     * @param username 用户名
     * @return 剩余次数
     */
    public int getRemainingAttempts(String username) {
        String key = KEY_PREFIX + username;
        String value = redisTemplate.opsForValue().get(key);
        int attempts = 0;
        if (value != null) {
            try {
                attempts = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                attempts = 0;
            }
        }
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }

    /**
     * 记录登录失败
     *
     * @param username 用户名
     */
    public void recordFailedAttempt(String username) {
        String key = KEY_PREFIX + username;
        
        // 先设置初始值并设置过期时间（如果 key 不存在）
        redisTemplate.opsForValue().setIfAbsent(
                key, "0", Duration.ofMinutes(LOCK_TIME_MINUTES));
        
        // 增加计数
        redisTemplate.opsForValue().increment(key);
    }

    /**
     * 清除失败记录
     *
     * @param username 用户名
     */
    public void clearFailedAttempts(String username) {
        String key = KEY_PREFIX + username;
        redisTemplate.delete(key);
    }
}
