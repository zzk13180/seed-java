package com.zhangzhankui.samples.common.security.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SecurityProperties 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("安全配置属性测试")
class SecurityPropertiesTest {

    @Mock
    private Environment environment;

    private SecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties(environment);
    }

    @Nested
    @DisplayName("JWT密钥验证测试")
    class JwtSecretValidationTest {

        @Test
        @DisplayName("未设置JWT密钥时应抛出异常")
        void shouldThrowException_WhenJwtSecretIsNull() {
            // Given
            securityProperties.setJwtSecret(null);
            securityProperties.setEnabled(true);
            when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});

            // When & Then
            assertThatThrownBy(() -> securityProperties.validateJwtSecret())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("JWT_SECRET environment variable is not set");
        }

        @Test
        @DisplayName("JWT密钥为空字符串时应抛出异常")
        void shouldThrowException_WhenJwtSecretIsBlank() {
            // Given
            securityProperties.setJwtSecret("   ");
            securityProperties.setEnabled(true);
            when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});

            // When & Then
            assertThatThrownBy(() -> securityProperties.validateJwtSecret())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("JWT_SECRET environment variable is not set");
        }

        @Test
        @DisplayName("生产环境使用默认密钥应抛出异常")
        void shouldThrowException_WhenUsingDefaultSecret_InProd() {
            // Given - 使用包含dev-only-default的默认密钥
            securityProperties.setJwtSecret("dev-only-default-jwt-secret-key-please-change-in-production-environment-12345");
            securityProperties.setEnabled(true);
            when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

            // When & Then
            assertThatThrownBy(() -> securityProperties.validateJwtSecret())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot use default JWT secret in production");
        }

        @Test
        @DisplayName("开发环境使用默认密钥应仅警告不抛异常")
        void shouldOnlyWarn_WhenUsingDefaultSecret_InDev() {
            // Given
            securityProperties.setJwtSecret("dev-only-default-jwt-secret-key-please-change-in-production-environment-12345");
            securityProperties.setEnabled(true);
            when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});

            // When & Then - 不应抛出异常
            assertThatCode(() -> securityProperties.validateJwtSecret())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("生产环境密钥长度不足应抛出异常")
        void shouldThrowException_WhenSecretTooShort_InProd() {
            // Given - 短于32字符
            securityProperties.setJwtSecret("short-key-12345678901234");
            securityProperties.setEnabled(true);
            when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

            // When & Then
            assertThatThrownBy(() -> securityProperties.validateJwtSecret())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("less than minimum required");
        }

        @Test
        @DisplayName("有效的Base64密钥应通过验证")
        void shouldPass_WhenValidBase64Secret() {
            // Given - openssl rand -base64 64 生成的密钥
            String validSecret = "dGhpcyBpcyBhIHZlcnkgbG9uZyBhbmQgc2VjdXJlIHNlY3JldCBrZXkgZm9yIGp3dCB0b2tlbiBnZW5lcmF0aW9u";
            securityProperties.setJwtSecret(validSecret);
            securityProperties.setEnabled(true);
            when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

            // When & Then
            assertThatCode(() -> securityProperties.validateJwtSecret())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("禁用安全时应跳过验证")
        void shouldSkipValidation_WhenSecurityDisabled() {
            // Given
            securityProperties.setJwtSecret(null);
            securityProperties.setEnabled(false);

            // When & Then
            assertThatCode(() -> securityProperties.validateJwtSecret())
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("不安全模式检测测试")
    class InsecurePatternTest {

        @ParameterizedTest
        @ValueSource(strings = {"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "11111111111111111111111111111111"})
        @DisplayName("生产环境重复字符密钥应抛出异常")
        void shouldThrowException_WhenRepeatingChars_InProd(String repeatingSecret) {
            // Given
            securityProperties.setJwtSecret(repeatingSecret);
            securityProperties.setEnabled(true);
            when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

            // When & Then
            assertThatThrownBy(() -> securityProperties.validateJwtSecret())
                    .isInstanceOf(IllegalStateException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345678901234567890123456789012", "01234567890123456789012345678901"})
        @DisplayName("生产环境连续数字密钥应触发警告或异常")
        void shouldWarnOrThrow_WhenSequentialNumbers_InProd(String sequentialSecret) {
            // Given
            securityProperties.setJwtSecret(sequentialSecret);
            securityProperties.setEnabled(true);
            when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

            // When & Then - 可能因熵值不足或模式匹配而失败
            assertThatThrownBy(() -> securityProperties.validateJwtSecret())
                    .isInstanceOf(IllegalStateException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"passwordmyverylongsecretkey12345", "secretkeythatisverylongandweak1"})
        @DisplayName("以password/secret开头的密钥应被检测")
        void shouldDetect_WhenStartsWithWeakWord(String weakSecret) {
            // Given
            securityProperties.setJwtSecret(weakSecret);
            securityProperties.setEnabled(true);
            when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

            // When & Then
            assertThatThrownBy(() -> securityProperties.validateJwtSecret())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("insecure pattern");
        }
    }

    @Nested
    @DisplayName("熵值计算测试")
    class EntropyCalculationTest {

        @Test
        @DisplayName("低熵值密钥应被检测")
        void shouldDetect_WhenLowEntropy() {
            // Given - 低熵值密钥 (重复模式，但包含大小写和数字避免被正则先匹配)
            securityProperties.setJwtSecret("AbAb1212AbAb1212AbAb1212AbAb1212");
            securityProperties.setEnabled(true);
            when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

            // When & Then - 熵值约 2.0，应该被检测
            assertThatThrownBy(() -> securityProperties.validateJwtSecret())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("entropy");
        }

        @Test
        @DisplayName("高熵值密钥应通过验证")
        void shouldPass_WhenHighEntropy() {
            // Given - 高熵值密钥 (随机生成)
            String highEntropySecret = "Kx9#mP2$vL5@nQ8&jR4*wT7!cY1%fH3^";
            securityProperties.setJwtSecret(highEntropySecret);
            securityProperties.setEnabled(true);
            when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

            // When & Then
            assertThatCode(() -> securityProperties.validateJwtSecret())
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("配置属性默认值测试")
    class DefaultValuesTest {

        @Test
        @DisplayName("默认JWT密钥应包含dev-only标识")
        void shouldHaveDefaultJwtSecret() {
            assertThat(securityProperties.getJwtSecret()).contains("dev-only-default");
        }

        @Test
        @DisplayName("默认JWT过期时间应为7200秒")
        void shouldHaveDefaultJwtExpiration() {
            assertThat(securityProperties.getJwtExpiration()).isEqualTo(7200L);
        }

        @Test
        @DisplayName("默认刷新Token过期时间应为604800秒")
        void shouldHaveDefaultRefreshTokenExpiration() {
            assertThat(securityProperties.getRefreshTokenExpiration()).isEqualTo(604800L);
        }

        @Test
        @DisplayName("默认Token请求头应为Authorization")
        void shouldHaveDefaultTokenHeader() {
            assertThat(securityProperties.getTokenHeader()).isEqualTo("Authorization");
        }

        @Test
        @DisplayName("默认Token前缀应为Bearer")
        void shouldHaveDefaultTokenPrefix() {
            assertThat(securityProperties.getTokenPrefix()).isEqualTo("Bearer ");
        }

        @Test
        @DisplayName("默认应启用安全")
        void shouldBeEnabledByDefault() {
            assertThat(securityProperties.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("默认忽略路径应为空列表")
        void shouldHaveEmptyIgnorePaths() {
            assertThat(securityProperties.getIgnorePaths()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Getter/Setter测试")
    class GetterSetterTest {

        @Test
        @DisplayName("设置和获取JWT密钥")
        void shouldSetAndGetJwtSecret() {
            String secret = "test-secret-key";
            securityProperties.setJwtSecret(secret);
            assertThat(securityProperties.getJwtSecret()).isEqualTo(secret);
        }

        @Test
        @DisplayName("设置和获取JWT过期时间")
        void shouldSetAndGetJwtExpiration() {
            securityProperties.setJwtExpiration(3600L);
            assertThat(securityProperties.getJwtExpiration()).isEqualTo(3600L);
        }

        @Test
        @DisplayName("设置和获取刷新Token过期时间")
        void shouldSetAndGetRefreshTokenExpiration() {
            securityProperties.setRefreshTokenExpiration(86400L);
            assertThat(securityProperties.getRefreshTokenExpiration()).isEqualTo(86400L);
        }
    }
}
