package com.zhangzhankui.samples.common.security.util;

import com.zhangzhankui.samples.common.security.config.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JwtUtils 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT工具类测试")
class JwtUtilsTest {

    @Mock
    private Environment environment;

    private JwtUtils jwtUtils;
    private SecurityProperties securityProperties;

    // 测试用的安全密钥 (Base64编码，至少256位)
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "this-is-a-very-long-and-secure-secret-key-for-testing-jwt-utils-class-12345".getBytes()
    );

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties(environment);
        securityProperties.setJwtSecret(TEST_SECRET);
        securityProperties.setJwtExpiration(7200L);
        securityProperties.setRefreshTokenExpiration(604800L);
        jwtUtils = new JwtUtils(securityProperties);
    }

    @Nested
    @DisplayName("Token生成测试")
    class TokenGenerationTest {

        @Test
        @DisplayName("生成Token应返回非空字符串")
        void generateToken_ShouldReturnNonEmptyString() {
            // When
            String token = jwtUtils.generateToken("testuser");

            // Then
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("生成Token应包含用户名")
        void generateToken_ShouldContainUsername() {
            // When
            String token = jwtUtils.generateToken("testuser");
            String extractedUsername = jwtUtils.extractUsername(token);

            // Then
            assertThat(extractedUsername).isEqualTo("testuser");
        }

        @Test
        @DisplayName("生成带额外声明的Token")
        void generateToken_WithExtraClaims() {
            // Given
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "ADMIN");

            // When
            String token = jwtUtils.generateToken("testuser", claims);

            // Then
            assertThat(token).isNotBlank();
            assertThat(jwtUtils.extractUsername(token)).isEqualTo("testuser");
        }

        @Test
        @DisplayName("生成刷新Token应返回非空字符串")
        void generateRefreshToken_ShouldReturnNonEmptyString() {
            // When
            String refreshToken = jwtUtils.generateRefreshToken("testuser");

            // Then
            assertThat(refreshToken).isNotBlank();
        }
    }

    @Nested
    @DisplayName("Token验证测试")
    class TokenValidationTest {

        @Test
        @DisplayName("有效Token应验证通过")
        void validateToken_ValidToken_ShouldReturnTrue() {
            // Given
            String token = jwtUtils.generateToken("testuser");

            // When
            boolean isValid = jwtUtils.validateToken(token);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("无效Token应验证失败")
        void validateToken_InvalidToken_ShouldReturnFalse() {
            // Given
            String invalidToken = "invalid.jwt.token";

            // When
            boolean isValid = jwtUtils.validateToken(invalidToken);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("用户名匹配时应验证通过")
        void validateToken_WithMatchingUsername_ShouldReturnTrue() {
            // Given
            String token = jwtUtils.generateToken("testuser");

            // When
            boolean isValid = jwtUtils.validateToken(token, "testuser");

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("用户名不匹配时应验证失败")
        void validateToken_WithMismatchedUsername_ShouldReturnFalse() {
            // Given
            String token = jwtUtils.generateToken("testuser");

            // When
            boolean isValid = jwtUtils.validateToken(token, "otheruser");

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("null用户名应验证失败")
        void validateToken_WithNullUsername_ShouldReturnFalse() {
            // Given
            String token = jwtUtils.generateToken("testuser");

            // When
            boolean isValid = jwtUtils.validateToken(token, null);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("无效Token与用户名验证应返回false而非抛异常")
        void validateToken_InvalidTokenWithUsername_ShouldReturnFalseNotThrow() {
            // Given
            String invalidToken = "invalid.jwt.token";

            // When & Then - 应该返回 false，不抛异常
            assertThatCode(() -> jwtUtils.validateToken(invalidToken, "testuser"))
                    .doesNotThrowAnyException();
            assertThat(jwtUtils.validateToken(invalidToken, "testuser")).isFalse();
        }
    }

    @Nested
    @DisplayName("Token提取测试")
    class TokenExtractionTest {

        @Test
        @DisplayName("从Token中提取用户名")
        void extractUsername_ShouldReturnUsername() {
            // Given
            String token = jwtUtils.generateToken("testuser");

            // When
            String username = jwtUtils.extractUsername(token);

            // Then
            assertThat(username).isEqualTo("testuser");
        }

        @Test
        @DisplayName("从Token中提取过期时间")
        void extractExpiration_ShouldReturnExpirationDate() {
            // Given
            String token = jwtUtils.generateToken("testuser");

            // When
            var expiration = jwtUtils.extractExpiration(token);

            // Then
            assertThat(expiration).isNotNull();
            assertThat(expiration).isAfter(new java.util.Date());
        }
    }

    @Nested
    @DisplayName("Token过期测试")
    class TokenExpirationTest {

        @Test
        @DisplayName("新生成的Token不应过期")
        void isTokenExpired_NewToken_ShouldReturnFalse() {
            // Given
            String token = jwtUtils.generateToken("testuser");

            // When
            boolean isExpired = jwtUtils.isTokenExpired(token);

            // Then
            assertThat(isExpired).isFalse();
        }
    }
}
