package com.zhangzhankui.seed.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.zhangzhankui.seed.auth.config.LoginSecurityConfig;
import com.zhangzhankui.seed.auth.domain.vo.LoginVO;
import com.zhangzhankui.seed.system.api.RemoteUserService;
import com.zhangzhankui.seed.system.api.dto.UserCredentialsDTO;
import com.zhangzhankui.seed.common.core.auth.AuthProvider;
import com.zhangzhankui.seed.common.core.domain.ApiResult;
import com.zhangzhankui.seed.common.core.domain.LoginUser;
import com.zhangzhankui.seed.common.core.exception.ServiceException;
import com.zhangzhankui.seed.common.core.exception.UserException;
import com.zhangzhankui.seed.common.redis.utils.RedisUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 登录服务单元测试
 *
 * <p>测试覆盖：
 * - 输入验证
 * - 远程凭据验证
 * - 账户锁定机制
 * - IP 限流
 * - 登录成功/失败流程
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginService 单元测试")
class LoginServiceTest {

    @Mock
    private RemoteUserService remoteUserService;

    @Mock
    private RedisUtils redisUtils;

    @Mock
    private LoginSecurityConfig securityConfig;

    @Mock
    private AuthProvider authProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginService loginService;

    private LoginUser mockLoginUser;
    private UserCredentialsDTO mockCredentials;

    @BeforeEach
    void setUp() {
        mockLoginUser = new LoginUser();
        mockLoginUser.setUserId(1L);
        mockLoginUser.setUsername("admin");
        mockLoginUser.setNickname("管理员");
        mockLoginUser.setPassword("$2a$10$encrypted_password_hash");
        mockLoginUser.setRoles(new HashSet<>(Arrays.asList("admin", "user")));

        mockCredentials = new UserCredentialsDTO(
            1L, "admin", "管理员", "$2a$10$encrypted_password_hash",
            0, null,
            new HashSet<>(Arrays.asList("admin", "user")),
            new HashSet<>()
        );

        // 默认 securityConfig 配置 - 防止意外触发账户锁定
        lenient().when(securityConfig.getMaxFailAttempts()).thenReturn(5);
        lenient().when(securityConfig.getLockDuration()).thenReturn(30);
        lenient().when(securityConfig.getFailCountResetDuration()).thenReturn(30);
        lenient().when(securityConfig.isIpRateLimitEnabled()).thenReturn(false);
        lenient().when(securityConfig.isLoginLogEnabled()).thenReturn(false);
    }

    @Nested
    @DisplayName("输入验证测试")
    class InputValidationTests {

        @Test
        @DisplayName("用户名为null时抛出异常")
        void shouldThrowExceptionWhenUsernameIsNull() {
            assertThatThrownBy(() -> loginService.login(null, "password"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("用户名不能为空");
        }

        @Test
        @DisplayName("密码为null时抛出异常")
        void shouldThrowExceptionWhenPasswordIsNull() {
            assertThatThrownBy(() -> loginService.login("admin", null))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("密码不能为空");
        }

        @Test
        @DisplayName("用户名为空字符串时抛出异常")
        void shouldThrowExceptionWhenUsernameIsEmpty() {
            assertThatThrownBy(() -> loginService.login("", "password"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("用户名不能为空");
        }

        @Test
        @DisplayName("密码为空字符串时抛出异常")
        void shouldThrowExceptionWhenPasswordIsEmpty() {
            assertThatThrownBy(() -> loginService.login("admin", ""))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("密码不能为空");
        }

        @Test
        @DisplayName("用户名仅含空格时抛出异常")
        void shouldThrowExceptionWhenUsernameIsOnlyWhitespace() {
            assertThatThrownBy(() -> loginService.login("   ", "password"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("用户名不能为空");
        }

        @Test
        @DisplayName("密码仅含空格时抛出异常")
        void shouldThrowExceptionWhenPasswordIsOnlyWhitespace() {
            assertThatThrownBy(() -> loginService.login("admin", "   "))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("密码不能为空");
        }
    }

    @Nested
    @DisplayName("远程凭据验证测试")
    class CredentialValidationTests {

        @Test
        @DisplayName("getUserCredentials 返回 FAIL 时抛出 UserException")
        void shouldThrowExceptionWhenCredentialsFail() {
            when(redisUtils.hasKey(anyString())).thenReturn(false);
            when(redisUtils.increment(anyString())).thenReturn(1L);
            when(remoteUserService.getUserCredentials(anyString()))
                .thenReturn(ApiResult.fail("用户不存在"));

            assertThatThrownBy(() -> loginService.login("admin", "wrong"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("用户不存在或密码错误");
        }

        @Test
        @DisplayName("getUserCredentials 返回成功但 data 为 null 时抛出 UserException")
        void shouldThrowExceptionWhenLoginUserIsNull() {
            when(redisUtils.hasKey(anyString())).thenReturn(false);
            when(redisUtils.increment(anyString())).thenReturn(1L);
            when(remoteUserService.getUserCredentials(anyString()))
                .thenReturn(ApiResult.ok(null));

            assertThatThrownBy(() -> loginService.login("nonexistent", "password"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("用户不存在或密码错误");
        }

        @Test
        @DisplayName("密码验证失败时抛出 UserException")
        void shouldThrowExceptionWhenPasswordMismatch() {
            when(redisUtils.hasKey(anyString())).thenReturn(false);
            when(redisUtils.increment(anyString())).thenReturn(1L);
            when(remoteUserService.getUserCredentials(anyString()))
                .thenReturn(ApiResult.ok(mockCredentials));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            assertThatThrownBy(() -> loginService.login("admin", "wrong"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("用户不存在或密码错误");
        }

        @Test
        @DisplayName("凭据验证失败时记录失败次数")
        void shouldRecordFailureWhenCredentialsFail() {
            when(redisUtils.hasKey(anyString())).thenReturn(false);
            when(redisUtils.increment(anyString())).thenReturn(1L);
            when(remoteUserService.getUserCredentials(anyString()))
                .thenReturn(ApiResult.fail("用户不存在"));

            try {
                loginService.login("admin", "wrong");
            } catch (UserException ignored) {}

            verify(redisUtils, atLeastOnce()).increment(contains("pwd_err_cnt:"));
        }
    }

    @Nested
    @DisplayName("登录成功测试")
    class LoginSuccessTests {

        @Test
        @DisplayName("凭据验证通过时返回 LoginVO")
        void shouldReturnLoginVOWhenCredentialsAreValid() {
            when(redisUtils.hasKey(anyString())).thenReturn(false);
            when(remoteUserService.getUserCredentials(anyString()))
                .thenReturn(ApiResult.ok(mockCredentials));
            when(passwordEncoder.matches("admin123", "$2a$10$encrypted_password_hash")).thenReturn(true);
            when(authProvider.login(any(LoginUser.class))).thenReturn("test-token-123");

            LoginVO result = loginService.login("admin", "admin123");

            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo("test-token-123");
            assertThat(result.tokenName()).isEqualTo("Authorization");
            assertThat(result.user().getUsername()).isEqualTo("admin");
        }

        @Test
        @DisplayName("登录成功时清除失败记录")
        void shouldClearFailureCountOnSuccess() {
            when(redisUtils.hasKey(anyString())).thenReturn(false);
            when(remoteUserService.getUserCredentials(anyString()))
                .thenReturn(ApiResult.ok(mockCredentials));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(authProvider.login(any(LoginUser.class))).thenReturn("token");

            loginService.login("admin", "admin123");

            verify(redisUtils).delete(contains("pwd_err_cnt:admin"));
        }
    }

    @Nested
    @DisplayName("账户锁定测试")
    class AccountLockTests {

        @Test
        @DisplayName("账户被锁定时抛出异常")
        void shouldThrowExceptionWhenAccountIsLocked() {
            when(redisUtils.hasKey(contains("pwd_err_cnt:lock:admin"))).thenReturn(true);
            when(redisUtils.getExpire(contains("pwd_err_cnt:lock:admin"))).thenReturn(1800L);

            assertThatThrownBy(() -> loginService.login("admin", "password"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("锁定");
        }
    }

    @Nested
    @DisplayName("IP限流测试")
    class IpRateLimitTests {

        @Test
        @DisplayName("IP超过限流阈值时抛出异常")
        void shouldThrowExceptionWhenIpExceedsRateLimit() {
            when(securityConfig.isIpRateLimitEnabled()).thenReturn(true);
            when(securityConfig.getIpRateLimitMax()).thenReturn(10);
            when(redisUtils.increment(anyString())).thenReturn(11L);

            assertThatThrownBy(() -> loginService.login("admin", "password"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("请求过于频繁");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("超长用户名应正常处理")
        void shouldHandleVeryLongUsername() {
            String longUsername = "a".repeat(200);

            when(redisUtils.hasKey(anyString())).thenReturn(false);
            when(redisUtils.increment(anyString())).thenReturn(1L);
            when(remoteUserService.getUserCredentials(anyString()))
                .thenReturn(ApiResult.ok(null));

            assertThatThrownBy(() -> loginService.login(longUsername, "password"))
                .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("含特殊字符的用户名应正常处理")
        void shouldHandleSpecialCharactersInUsername() {
            String specialUsername = "user@domain.com";

            when(redisUtils.hasKey(anyString())).thenReturn(false);
            when(redisUtils.increment(anyString())).thenReturn(1L);
            when(remoteUserService.getUserCredentials(anyString()))
                .thenReturn(ApiResult.ok(null));

            assertThatThrownBy(() -> loginService.login(specialUsername, "password"))
                .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("Unicode用户名应正常处理")
        void shouldHandleUnicodeUsername() {
            String unicodeUsername = "用户名测试";

            when(redisUtils.hasKey(anyString())).thenReturn(false);
            when(redisUtils.increment(anyString())).thenReturn(1L);
            when(remoteUserService.getUserCredentials(anyString()))
                .thenReturn(ApiResult.ok(null));

            assertThatThrownBy(() -> loginService.login(unicodeUsername, "password"))
                .isInstanceOf(UserException.class);
        }
    }
}
