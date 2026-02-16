package com.zhangzhankui.seed.common.security.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.zhangzhankui.seed.common.core.auth.AuthProvider;
import com.zhangzhankui.seed.common.core.domain.LoginUser;
import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SecurityUtils 单元测试
 *
 * <p>通过反射注入 authProvider，测试真实委托逻辑。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityUtils 单元测试")
class SecurityUtilsTest {

  /** 通过反射设置 SecurityUtils 的 static authProvider 字段 */
  private static void setAuthProvider(AuthProvider provider) throws Exception {
    Field field = SecurityUtils.class.getDeclaredField("authProvider");
    field.setAccessible(true);
    field.set(null, provider);
  }

  @AfterEach
  void tearDown() throws Exception {
    // 恢复为 null，避免测试间污染
    setAuthProvider(null);
  }

  @Test
  @DisplayName("getUserId 应从 LoginUser 提取 userId")
  void shouldGetCurrentUserId() throws Exception {
    AuthProvider provider = Mockito.mock(AuthProvider.class);
    LoginUser loginUser = new LoginUser();
    loginUser.setUserId(123L);
    loginUser.setUsername("testUser");
    Mockito.when(provider.getLoginUser()).thenReturn(loginUser);
    setAuthProvider(provider);

    assertThat(SecurityUtils.getUserId()).isEqualTo(123L);
  }

  @Test
  @DisplayName("getUsername 应从 LoginUser 提取 username")
  void shouldGetCurrentUsername() throws Exception {
    AuthProvider provider = Mockito.mock(AuthProvider.class);
    LoginUser loginUser = new LoginUser();
    loginUser.setUsername("testUser");
    Mockito.when(provider.getLoginUser()).thenReturn(loginUser);
    setAuthProvider(provider);

    assertThat(SecurityUtils.getUsername()).isEqualTo("testUser");
  }

  @Test
  @DisplayName("用户未登录时 getUserId 和 getUsername 应返回 null")
  void shouldReturnNullWhenUserNotLoggedIn() throws Exception {
    AuthProvider provider = Mockito.mock(AuthProvider.class);
    Mockito.when(provider.getLoginUser()).thenReturn(null);
    setAuthProvider(provider);

    assertThat(SecurityUtils.getUserId()).isNull();
    assertThat(SecurityUtils.getUsername()).isNull();
  }

  @Test
  @DisplayName("authProvider 为 null 时 getLoginUser 应返回 null")
  void shouldReturnNullWhenAuthProviderIsNull() {
    // authProvider is null by default after tearDown
    assertThat(SecurityUtils.getLoginUser()).isNull();
    assertThat(SecurityUtils.getUserId()).isNull();
    assertThat(SecurityUtils.getUsername()).isNull();
  }

  @Test
  @DisplayName("getTenantId 应从 LoginUser 提取 tenantId")
  void shouldGetTenantId() throws Exception {
    AuthProvider provider = Mockito.mock(AuthProvider.class);
    LoginUser loginUser = new LoginUser();
    loginUser.setTenantId("tenant123");
    Mockito.when(provider.getLoginUser()).thenReturn(loginUser);
    setAuthProvider(provider);

    assertThat(SecurityUtils.getTenantId()).isEqualTo("tenant123");
  }

  @Test
  @DisplayName("用户未登录时 getTenantId 应返回 null")
  void shouldReturnNullTenantIdWhenNotLoggedIn() throws Exception {
    AuthProvider provider = Mockito.mock(AuthProvider.class);
    Mockito.when(provider.getLoginUser()).thenReturn(null);
    setAuthProvider(provider);

    assertThat(SecurityUtils.getTenantId()).isNull();
  }

  @Test
  @DisplayName("isLogin 应委托给 authProvider.isLogin")
  void shouldDelegateIsLoginToAuthProvider() throws Exception {
    AuthProvider provider = Mockito.mock(AuthProvider.class);
    Mockito.when(provider.isLogin()).thenReturn(true);
    setAuthProvider(provider);

    assertThat(SecurityUtils.isLogin()).isTrue();
    Mockito.verify(provider).isLogin();
  }

  @Test
  @DisplayName("authProvider 为 null 时 isLogin 应返回 false")
  void shouldReturnFalseWhenAuthProviderIsNull() {
    assertThat(SecurityUtils.isLogin()).isFalse();
  }

  @Test
  @DisplayName("hasPermission 应委托给 authProvider.hasPermission")
  void shouldDelegateHasPermissionToAuthProvider() throws Exception {
    AuthProvider provider = Mockito.mock(AuthProvider.class);
    Mockito.when(provider.hasPermission("test:perm")).thenReturn(true);
    setAuthProvider(provider);

    assertThat(SecurityUtils.hasPermission("test:perm")).isTrue();
    Mockito.verify(provider).hasPermission("test:perm");
  }

  @Test
  @DisplayName("hasRole 应委托给 authProvider.hasRole")
  void shouldDelegateHasRoleToAuthProvider() throws Exception {
    AuthProvider provider = Mockito.mock(AuthProvider.class);
    Mockito.when(provider.hasRole("ADMIN")).thenReturn(true);
    setAuthProvider(provider);

    assertThat(SecurityUtils.hasRole("ADMIN")).isTrue();
    Mockito.verify(provider).hasRole("ADMIN");
  }

  @Test
  @DisplayName("authProvider 为 null 时 getToken 应返回 null")
  void shouldReturnNullTokenWhenAuthProviderIsNull() {
    assertThat(SecurityUtils.getToken()).isNull();
  }

  @Test
  @DisplayName("getToken 应委托给 authProvider.getToken")
  void shouldDelegateGetTokenToAuthProvider() throws Exception {
    AuthProvider provider = Mockito.mock(AuthProvider.class);
    Mockito.when(provider.getToken()).thenReturn("abc-token");
    setAuthProvider(provider);

    assertThat(SecurityUtils.getToken()).isEqualTo("abc-token");
    Mockito.verify(provider).getToken();
  }

  @Test
  @DisplayName("用户名包含特殊字符应正常提取")
  void shouldHandleUsernameWithSpecialChars() throws Exception {
    AuthProvider provider = Mockito.mock(AuthProvider.class);
    LoginUser loginUser = new LoginUser();
    loginUser.setUsername("user@domain.com");
    Mockito.when(provider.getLoginUser()).thenReturn(loginUser);
    setAuthProvider(provider);

    assertThat(SecurityUtils.getUsername()).isEqualTo("user@domain.com");
  }

  @Test
  @DisplayName("超大用户ID应正常提取")
  void shouldHandleVeryLargeUserId() throws Exception {
    AuthProvider provider = Mockito.mock(AuthProvider.class);
    LoginUser loginUser = new LoginUser();
    loginUser.setUserId(Long.MAX_VALUE);
    Mockito.when(provider.getLoginUser()).thenReturn(loginUser);
    setAuthProvider(provider);

    assertThat(SecurityUtils.getUserId()).isEqualTo(Long.MAX_VALUE);
  }
}
