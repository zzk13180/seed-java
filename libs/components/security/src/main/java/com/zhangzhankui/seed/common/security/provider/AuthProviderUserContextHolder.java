package com.zhangzhankui.seed.common.security.provider;

import com.zhangzhankui.seed.common.core.auth.AuthProvider;
import com.zhangzhankui.seed.common.core.context.UserContextHolder;
import com.zhangzhankui.seed.common.core.domain.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于 AuthProvider 的用户上下文实现
 *
 * <p>统一的用户上下文，委托给当前激活的 AuthProvider
 */
@Slf4j
@RequiredArgsConstructor
public class AuthProviderUserContextHolder implements UserContextHolder {

  private final AuthProvider authProvider;

  @Override
  public Long getUserId() {
    try {
      LoginUser loginUser = authProvider.getLoginUser();
      return loginUser != null ? loginUser.getUserId() : null;
    } catch (Exception e) {
      log.debug("获取用户ID失败: {}", e.getMessage());
      return null;
    }
  }

  @Override
  public String getUsername() {
    try {
      LoginUser loginUser = authProvider.getLoginUser();
      return loginUser != null ? loginUser.getUsername() : null;
    } catch (Exception e) {
      log.debug("获取用户名失败: {}", e.getMessage());
      return null;
    }
  }

  @Override
  public String getNickname() {
    try {
      LoginUser loginUser = authProvider.getLoginUser();
      return loginUser != null ? loginUser.getNickname() : null;
    } catch (Exception e) {
      log.debug("获取昵称失败: {}", e.getMessage());
      return null;
    }
  }

  @Override
  public String getTenantId() {
    try {
      LoginUser loginUser = authProvider.getLoginUser();
      return loginUser != null ? loginUser.getTenantId() : null;
    } catch (Exception e) {
      log.debug("获取租户ID失败: {}", e.getMessage());
      return null;
    }
  }

  @Override
  public boolean isLogin() {
    return authProvider.isLogin();
  }
}
