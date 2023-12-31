package com.zhangzhankui.samples.common.security.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * LoginAttemptLimiter 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("登录尝试限制器测试")
class LoginAttemptLimiterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private LoginAttemptLimiter loginAttemptLimiter;

    private static final String TEST_USERNAME = "testuser";
    private static final String KEY_PREFIX = "login:attempt:";

    @BeforeEach
    void setUp() {
        loginAttemptLimiter = new LoginAttemptLimiter(redisTemplate);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("isBlocked 测试")
    class IsBlockedTest {

        @Test
        @DisplayName("无记录时不应被锁定")
        void isBlocked_NoRecord_ShouldReturnFalse() {
            // Given
            when(valueOperations.get(KEY_PREFIX + TEST_USERNAME)).thenReturn(null);

            // When
            boolean blocked = loginAttemptLimiter.isBlocked(TEST_USERNAME);

            // Then
            assertThat(blocked).isFalse();
        }

        @Test
        @DisplayName("尝试次数小于5次不应被锁定")
        void isBlocked_LessThan5Attempts_ShouldReturnFalse() {
            // Given
            when(valueOperations.get(KEY_PREFIX + TEST_USERNAME)).thenReturn("4");

            // When
            boolean blocked = loginAttemptLimiter.isBlocked(TEST_USERNAME);

            // Then
            assertThat(blocked).isFalse();
        }

        @Test
        @DisplayName("尝试次数达到5次应被锁定")
        void isBlocked_5Attempts_ShouldReturnTrue() {
            // Given
            when(valueOperations.get(KEY_PREFIX + TEST_USERNAME)).thenReturn("5");

            // When
            boolean blocked = loginAttemptLimiter.isBlocked(TEST_USERNAME);

            // Then
            assertThat(blocked).isTrue();
        }

        @Test
        @DisplayName("尝试次数超过5次应被锁定")
        void isBlocked_MoreThan5Attempts_ShouldReturnTrue() {
            // Given
            when(valueOperations.get(KEY_PREFIX + TEST_USERNAME)).thenReturn("10");

            // When
            boolean blocked = loginAttemptLimiter.isBlocked(TEST_USERNAME);

            // Then
            assertThat(blocked).isTrue();
        }

        @Test
        @DisplayName("非数字值不应导致锁定")
        void isBlocked_InvalidValue_ShouldReturnFalse() {
            // Given
            when(valueOperations.get(KEY_PREFIX + TEST_USERNAME)).thenReturn("invalid");

            // When
            boolean blocked = loginAttemptLimiter.isBlocked(TEST_USERNAME);

            // Then
            assertThat(blocked).isFalse();
        }
    }

    @Nested
    @DisplayName("getRemainingLockTime 测试")
    class GetRemainingLockTimeTest {

        @Test
        @DisplayName("有过期时间应返回剩余秒数")
        void getRemainingLockTime_WithExpire_ShouldReturnSeconds() {
            // Given
            when(redisTemplate.getExpire(KEY_PREFIX + TEST_USERNAME, TimeUnit.SECONDS))
                    .thenReturn(600L);

            // When
            long remaining = loginAttemptLimiter.getRemainingLockTime(TEST_USERNAME);

            // Then
            assertThat(remaining).isEqualTo(600L);
        }

        @Test
        @DisplayName("无过期时间应返回0")
        void getRemainingLockTime_NoExpire_ShouldReturnZero() {
            // Given
            when(redisTemplate.getExpire(KEY_PREFIX + TEST_USERNAME, TimeUnit.SECONDS))
                    .thenReturn(-1L);

            // When
            long remaining = loginAttemptLimiter.getRemainingLockTime(TEST_USERNAME);

            // Then
            assertThat(remaining).isEqualTo(0L);
        }

        @Test
        @DisplayName("key不存在应返回0")
        void getRemainingLockTime_KeyNotExist_ShouldReturnZero() {
            // Given
            when(redisTemplate.getExpire(KEY_PREFIX + TEST_USERNAME, TimeUnit.SECONDS))
                    .thenReturn(null);

            // When
            long remaining = loginAttemptLimiter.getRemainingLockTime(TEST_USERNAME);

            // Then
            assertThat(remaining).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("getRemainingAttempts 测试")
    class GetRemainingAttemptsTest {

        @Test
        @DisplayName("无记录应返回5次")
        void getRemainingAttempts_NoRecord_ShouldReturn5() {
            // Given
            when(valueOperations.get(KEY_PREFIX + TEST_USERNAME)).thenReturn(null);

            // When
            int remaining = loginAttemptLimiter.getRemainingAttempts(TEST_USERNAME);

            // Then
            assertThat(remaining).isEqualTo(5);
        }

        @Test
        @DisplayName("已尝试3次应返回2次")
        void getRemainingAttempts_3Attempts_ShouldReturn2() {
            // Given
            when(valueOperations.get(KEY_PREFIX + TEST_USERNAME)).thenReturn("3");

            // When
            int remaining = loginAttemptLimiter.getRemainingAttempts(TEST_USERNAME);

            // Then
            assertThat(remaining).isEqualTo(2);
        }

        @Test
        @DisplayName("已尝试5次应返回0次")
        void getRemainingAttempts_5Attempts_ShouldReturn0() {
            // Given
            when(valueOperations.get(KEY_PREFIX + TEST_USERNAME)).thenReturn("5");

            // When
            int remaining = loginAttemptLimiter.getRemainingAttempts(TEST_USERNAME);

            // Then
            assertThat(remaining).isEqualTo(0);
        }

        @Test
        @DisplayName("超过5次应返回0次")
        void getRemainingAttempts_MoreThan5_ShouldReturn0() {
            // Given
            when(valueOperations.get(KEY_PREFIX + TEST_USERNAME)).thenReturn("10");

            // When
            int remaining = loginAttemptLimiter.getRemainingAttempts(TEST_USERNAME);

            // Then
            assertThat(remaining).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("recordFailedAttempt 测试")
    class RecordFailedAttemptTest {

        @Test
        @DisplayName("首次失败应设置初始值并增加计数")
        void recordFailedAttempt_FirstAttempt_ShouldSetAndIncrement() {
            // Given
            when(valueOperations.setIfAbsent(eq(KEY_PREFIX + TEST_USERNAME), eq("0"), 
                    any(Duration.class))).thenReturn(true);

            // When
            loginAttemptLimiter.recordFailedAttempt(TEST_USERNAME);

            // Then
            verify(valueOperations).setIfAbsent(eq(KEY_PREFIX + TEST_USERNAME), eq("0"), 
                    any(Duration.class));
            verify(valueOperations).increment(KEY_PREFIX + TEST_USERNAME);
        }

        @Test
        @DisplayName("重复失败应只增加计数")
        void recordFailedAttempt_SubsequentAttempt_ShouldOnlyIncrement() {
            // Given
            when(valueOperations.setIfAbsent(eq(KEY_PREFIX + TEST_USERNAME), eq("0"), 
                    any(Duration.class))).thenReturn(false);

            // When
            loginAttemptLimiter.recordFailedAttempt(TEST_USERNAME);

            // Then
            verify(valueOperations).setIfAbsent(eq(KEY_PREFIX + TEST_USERNAME), eq("0"), 
                    any(Duration.class));
            verify(valueOperations).increment(KEY_PREFIX + TEST_USERNAME);
        }
    }

    @Nested
    @DisplayName("clearFailedAttempts 测试")
    class ClearFailedAttemptsTest {

        @Test
        @DisplayName("清除失败记录应删除key")
        void clearFailedAttempts_ShouldDeleteKey() {
            // When
            loginAttemptLimiter.clearFailedAttempts(TEST_USERNAME);

            // Then
            verify(redisTemplate).delete(KEY_PREFIX + TEST_USERNAME);
        }
    }
}
