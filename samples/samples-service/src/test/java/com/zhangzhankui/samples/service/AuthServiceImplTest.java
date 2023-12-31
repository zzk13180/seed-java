package com.zhangzhankui.samples.service;

import com.zhangzhankui.samples.api.dto.LoginDTO;
import com.zhangzhankui.samples.api.vo.LoginVO;
import com.zhangzhankui.samples.common.core.exception.BusinessException;
import com.zhangzhankui.samples.common.security.config.SecurityProperties;
import com.zhangzhankui.samples.common.security.domain.LoginUser;
import com.zhangzhankui.samples.common.security.util.JwtUtils;
import com.zhangzhankui.samples.common.security.util.LoginAttemptLimiter;
import com.zhangzhankui.samples.db.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 认证服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务测试")
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginAttemptLimiter loginAttemptLimiter;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginDTO loginDTO;
    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");

        loginUser = new LoginUser();
        loginUser.setUserId(1L);
        loginUser.setUsername("testuser");
        loginUser.setPassword("password123");
        loginUser.setRoles(Collections.singleton("USER"));
        loginUser.setPermissions(Collections.emptySet());
    }

    @Test
    @DisplayName("登录 - 成功")
    void login_Success() {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                loginUser, null, loginUser.getAuthorities());

        when(loginAttemptLimiter.isBlocked(anyString())).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.generateToken(anyString())).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(anyString())).thenReturn("refresh-token");
        when(securityProperties.getJwtExpiration()).thenReturn(7200L);

        // When
        LoginVO result = authService.login(loginDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getExpiresIn()).isEqualTo(7200L);
        verify(loginAttemptLimiter).clearFailedAttempts("testuser");
    }

    @Test
    @DisplayName("登录 - 用户被锁定")
    void login_UserBlocked() {
        // Given
        when(loginAttemptLimiter.isBlocked("testuser")).thenReturn(true);
        when(loginAttemptLimiter.getRemainingLockTime("testuser")).thenReturn(900L);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("登录失败次数过多");
    }

    @Test
    @DisplayName("登录 - 密码错误")
    void login_BadCredentials() {
        // Given
        when(loginAttemptLimiter.isBlocked(anyString())).thenReturn(false);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        when(loginAttemptLimiter.getRemainingAttempts("testuser")).thenReturn(4);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("密码错误");
        
        verify(loginAttemptLimiter).recordFailedAttempt("testuser");
    }

    @Test
    @DisplayName("登录 - 超过最大尝试次数")
    void login_MaxAttemptsExceeded() {
        // Given
        when(loginAttemptLimiter.isBlocked(anyString())).thenReturn(false);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        when(loginAttemptLimiter.getRemainingAttempts("testuser")).thenReturn(0);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("锁定");
        
        verify(loginAttemptLimiter).recordFailedAttempt("testuser");
    }

    @Test
    @DisplayName("刷新Token - 成功")
    void refreshToken_Success() {
        // Given
        String oldRefreshToken = "old-refresh-token";
        when(jwtUtils.validateToken(oldRefreshToken)).thenReturn(true);
        when(jwtUtils.extractUsername(oldRefreshToken)).thenReturn("testuser");
        when(jwtUtils.generateToken("testuser")).thenReturn("new-access-token");
        when(jwtUtils.generateRefreshToken("testuser")).thenReturn("new-refresh-token");
        when(securityProperties.getJwtExpiration()).thenReturn(7200L);
        
        com.zhangzhankui.samples.db.entity.User user = new com.zhangzhankui.samples.db.entity.User();
        user.setId(1L);
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(user));

        // When
        LoginVO result = authService.refreshToken(oldRefreshToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("刷新Token - Token无效")
    void refreshToken_InvalidToken() {
        // Given
        String invalidToken = "invalid-token";
        when(jwtUtils.validateToken(invalidToken)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(invalidToken))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("刷新Token - Token中无用户名")
    void refreshToken_NullUsername() {
        // Given
        String tokenWithoutUsername = "token-without-username";
        when(jwtUtils.validateToken(tokenWithoutUsername)).thenReturn(true);
        when(jwtUtils.extractUsername(tokenWithoutUsername)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(tokenWithoutUsername))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Token");
    }

    @Test
    @DisplayName("刷新Token - Token中用户名为空字符串")
    void refreshToken_BlankUsername() {
        // Given
        String tokenWithBlankUsername = "token-with-blank-username";
        when(jwtUtils.validateToken(tokenWithBlankUsername)).thenReturn(true);
        when(jwtUtils.extractUsername(tokenWithBlankUsername)).thenReturn("   ");

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(tokenWithBlankUsername))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Token");
    }
}
