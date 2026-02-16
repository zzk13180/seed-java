package com.zhangzhankui.seed.common.security.provider;

import java.util.Collection;

import com.zhangzhankui.seed.common.core.auth.AuthProvider;
import com.zhangzhankui.seed.common.core.constant.SecurityConstants;
import com.zhangzhankui.seed.common.core.domain.LoginUser;
import com.zhangzhankui.seed.common.core.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * OAuth2/OIDC 认证提供者实现
 *
 * <p>基于 Spring Security OAuth2 实现，支持：
 *
 * <ul>
 *   <li>Logto、Keycloak、Auth0 等 OIDC Provider
 *   <li>JWT Token 验证
 *   <li>自动从 JWT claims 提取用户信息
 * </ul>
 *
 * <p>注意：OAuth2 场景下，登录/登出由外部 Provider 处理， 此实现主要用于 Resource Server 验证已有 Token
 */
@Slf4j
public class OAuth2AuthProvider implements AuthProvider {

  public static final String NAME = "oauth2";

  @Override
  public String getName() {
    return NAME;
  }

  /**
   * OAuth2 场景不支持直接登录，登录由外部 Provider 处理
   *
   * @param loginUser 登录用户信息（未使用）
   * @return 当前 Token 或 null
   */
  @Override
  public String login(LoginUser loginUser) {
    log.warn("OAuth2 模式不支持直接登录，请使用 OAuth2 Provider 登录流程");
    // 返回当前 token（如果已登录），而不是抛异常，避免违反里氏替换原则
    return getToken();
  }

  @Override
  public void logout() {
    // 清除本地 SecurityContext
    SecurityContextHolder.clearContext();
    log.debug("OAuth2 本地 SecurityContext 已清除");
    // 实际登出需要重定向到 OIDC Provider 的 logout endpoint
  }

  @Override
  public boolean isLogin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
  }

  @Override
  public LoginUser getLoginUser() {
    Jwt jwt = getJwt();
    if (jwt == null) {
      // 尝试从请求头获取（网关转发场景）
      return getLoginUserFromHeaders();
    }

    LoginUser loginUser = new LoginUser();
    // 从 JWT 提取用户信息
    loginUser.setUserId(parseUserId(jwt.getSubject()));
    loginUser.setUsername(getClaimAsString(jwt, "preferred_username", "username"));
    loginUser.setNickname(jwt.getClaimAsString("name"));
    loginUser.setTenantId(jwt.getClaimAsString("tenant_id"));
    loginUser.setToken(jwt.getTokenValue());

    return loginUser;
  }

  @Override
  public String getToken() {
    Jwt jwt = getJwt();
    return jwt != null ? jwt.getTokenValue() : null;
  }

  /** OAuth2 场景下，Token 验证由 Spring Security 自动处理 */
  @Override
  public boolean validateToken(String token) {
    // 实际验证由 Spring Security OAuth2 Resource Server 处理
    return isLogin();
  }

  /** OAuth2 场景不支持刷新 Token，由 OAuth2 Client 处理 */
  @Override
  public void refreshToken(long timeout) {
    log.debug("OAuth2 模式 Token 刷新由 OAuth2 Client 自动处理");
  }

  @Override
  public boolean hasPermission(String permission) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      return false;
    }
    return hasAuthority(auth.getAuthorities(), permission);
  }

  @Override
  public boolean hasRole(String role) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      return false;
    }
    // 角色通常带 ROLE_ 前缀
    return hasAuthority(auth.getAuthorities(), role)
        || hasAuthority(auth.getAuthorities(), "ROLE_" + role);
  }

  // ========== 辅助方法 ==========

  /** 获取当前 JWT */
  public Jwt getJwt() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      return jwtAuth.getToken();
    }
    return null;
  }

  /** 从请求头获取用户信息（网关转发场景） */
  private LoginUser getLoginUserFromHeaders() {
    String userId = ServletUtils.getHeader(SecurityConstants.HEADER_USER_ID);
    if (userId == null) {
      return null;
    }

    LoginUser loginUser = new LoginUser();
    loginUser.setUserId(parseUserId(userId));
    loginUser.setUsername(ServletUtils.getHeader(SecurityConstants.HEADER_USERNAME));
    loginUser.setTenantId(ServletUtils.getHeader(SecurityConstants.HEADER_TENANT_ID));
    return loginUser;
  }

  /** 解析用户ID */
  private Long parseUserId(String userId) {
    if (userId == null) {
      return null;
    }
    try {
      return Long.parseLong(userId);
    } catch (NumberFormatException e) {
      // OIDC Provider 的 sub 可能不是数字
      log.debug("用户ID不是数字格式: {}", userId);
      return null;
    }
  }

  /** 从 JWT 获取 claim，支持多个候选名称 */
  private String getClaimAsString(Jwt jwt, String... claimNames) {
    for (String name : claimNames) {
      String value = jwt.getClaimAsString(name);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  /** 检查是否有指定权限 */
  private boolean hasAuthority(
      Collection<? extends GrantedAuthority> authorities, String authority) {
    return authorities.stream().anyMatch(a -> a.getAuthority().equals(authority));
  }

  /** 判断是否是内部请求 */
  public boolean isInnerRequest() {
    String source = ServletUtils.getHeader(SecurityConstants.HEADER_FROM_SOURCE);
    return SecurityConstants.INNER.equals(source);
  }
}
