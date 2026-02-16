package com.zhangzhankui.seed.common.security.utils;

import com.zhangzhankui.seed.common.core.auth.AuthProvider;
import com.zhangzhankui.seed.common.core.constant.SecurityConstants;
import com.zhangzhankui.seed.common.core.domain.LoginUser;
import com.zhangzhankui.seed.common.core.utils.ServletUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 安全工具类
 *
 * <p>统一的安全工具，自动适配当前激活的认证方案：
 *
 * <ul>
 *   <li>Sa-Token 模式 - 从 Sa-Token Session 获取信息
 *   <li>OAuth2 模式 - 从 JWT Token 或请求头获取信息
 * </ul>
 */
@Component
public class SecurityUtils implements ApplicationContextAware {

  private static volatile AuthProvider authProvider;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SecurityUtils.authProvider = applicationContext.getBean(AuthProvider.class);
  }

  /** 获取当前 AuthProvider */
  public static AuthProvider getAuthProvider() {
    return authProvider;
  }

  /** 获取当前登录用户ID */
  public static Long getUserId() {
    LoginUser loginUser = getLoginUser();
    return loginUser != null ? loginUser.getUserId() : null;
  }

  /** 获取当前登录用户名 */
  public static String getUsername() {
    LoginUser loginUser = getLoginUser();
    return loginUser != null ? loginUser.getUsername() : null;
  }

  /** 获取当前登录用户 */
  public static LoginUser getLoginUser() {
    if (authProvider != null) {
      return authProvider.getLoginUser();
    }
    return null;
  }

  /** 设置登录用户（仅 Sa-Token 模式有效） */
  public static void setLoginUser(LoginUser loginUser) {
    if (authProvider != null) {
      authProvider.login(loginUser);
    }
  }

  /** 判断是否登录 */
  public static boolean isLogin() {
    return authProvider != null && authProvider.isLogin();
  }

  /** 获取 Token */
  public static String getToken() {
    return authProvider != null ? authProvider.getToken() : null;
  }

  /** 判断是否有权限 */
  public static boolean hasPermission(String permission) {
    return authProvider != null && authProvider.hasPermission(permission);
  }

  /** 判断是否有角色 */
  public static boolean hasRole(String role) {
    return authProvider != null && authProvider.hasRole(role);
  }

  /** 获取租户ID */
  public static String getTenantId() {
    LoginUser loginUser = getLoginUser();
    return loginUser != null ? loginUser.getTenantId() : null;
  }

  /**
   * 判断是否是内部请求（仅检查 header 标识）
   *
   * <p>注意：此方法仅检查 X-From-Source header，不验证 HMAC 签名。 完整的内部认证验证由 {@code @InnerAuth} 注解 +
   * {@code InnerAuthAspect} 切面处理。
   */
  public static boolean isInnerRequest() {
    String source = ServletUtils.getHeader(SecurityConstants.HEADER_FROM_SOURCE);
    return SecurityConstants.INNER.equals(source);
  }

  /** 获取请求头中的用户ID（网关转发场景） */
  public static Long getHeaderUserId() {
    String userId = ServletUtils.getHeader(SecurityConstants.HEADER_USER_ID);
    if (userId == null) {
      return null;
    }
    try {
      return Long.parseLong(userId);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
