package com.zhangzhankui.seed.common.security.provider;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.stp.StpUtil;
import com.zhangzhankui.seed.common.core.auth.AuthProvider;
import com.zhangzhankui.seed.common.core.constant.SecurityConstants;
import com.zhangzhankui.seed.common.core.domain.LoginUser;
import lombok.extern.slf4j.Slf4j;

/**
 * Sa-Token 认证提供者实现
 *
 * <p>基于 Sa-Token 实现的认证能力，支持：
 *
 * <ul>
 *   <li>分布式 Session（Redis 存储）
 *   <li>Token 自动续期
 *   <li>多端登录管理
 *   <li>权限/角色校验
 * </ul>
 */
@Slf4j
public class SaTokenAuthProvider implements AuthProvider {

  public static final String NAME = "satoken";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String login(LoginUser loginUser) {
    // Sa-Token 登录
    StpUtil.login(loginUser.getUserId());
    // 将用户信息存入 Session
    StpUtil.getSession().set(SecurityConstants.LOGIN_USER, loginUser);
    log.debug("Sa-Token 登录成功: userId={}", loginUser.getUserId());
    return StpUtil.getTokenValue();
  }

  @Override
  public void logout() {
    if (StpUtil.isLogin()) {
      StpUtil.logout();
      log.debug("Sa-Token 登出成功");
    }
  }

  @Override
  public boolean isLogin() {
    try {
      return StpUtil.isLogin();
    } catch (Exception e) {
      log.warn("检查登录状态时发生异常: {}", e.getMessage(), e);
      return false;
    }
  }

  @Override
  public LoginUser getLoginUser() {
    try {
      if (StpUtil.isLogin()) {
        return (LoginUser) StpUtil.getSession().get(SecurityConstants.LOGIN_USER);
      }
    } catch (Exception e) {
      log.debug("获取登录用户失败: {}", e.getMessage());
    }
    return null;
  }

  @Override
  public String getToken() {
    try {
      return StpUtil.getTokenValue();
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public boolean validateToken(String token) {
    try {
      Object loginId = StpUtil.getLoginIdByToken(token);
      return loginId != null;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void refreshToken(long timeout) {
    if (StpUtil.isLogin()) {
      StpUtil.renewTimeout(timeout);
    }
  }

  @Override
  public boolean hasPermission(String permission) {
    try {
      return StpUtil.hasPermission(permission);
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public boolean hasRole(String role) {
    try {
      return StpUtil.hasRole(role);
    } catch (Exception e) {
      return false;
    }
  }

  /** 获取请求头中的用户ID（网关转发场景） */
  public Long getHeaderUserId() {
    try {
      String userId = SaHolder.getRequest().getHeader(SecurityConstants.HEADER_USER_ID);
      return userId != null ? Long.parseLong(userId) : null;
    } catch (Exception e) {
      return null;
    }
  }

  /** 判断是否是内部请求 */
  public boolean isInnerRequest() {
    try {
      String source = SaHolder.getRequest().getHeader(SecurityConstants.HEADER_FROM_SOURCE);
      return SecurityConstants.INNER.equals(source);
    } catch (Exception e) {
      return false;
    }
  }
}
