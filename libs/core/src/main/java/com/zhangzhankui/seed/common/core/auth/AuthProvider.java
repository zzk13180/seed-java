package com.zhangzhankui.seed.common.core.auth;

import com.zhangzhankui.seed.common.core.domain.LoginUser;

/**
 * 认证提供者接口
 *
 * <p>抽象认证能力，支持多种认证方案：
 *
 * <ul>
 *   <li>Sa-Token - 自研 Token 管理
 *   <li>OAuth2/OIDC - 集成 Logto、Keycloak 等外部 Provider
 *   <li>JWT - 简单 JWT 验证
 * </ul>
 *
 * <p>设计原则：
 *
 * <ul>
 *   <li>接口最小化 - 只定义核心认证操作
 *   <li>实现可插拔 - 通过配置切换认证方案
 *   <li>解耦业务 - 业务模块无需关心具体认证实现
 * </ul>
 */
public interface AuthProvider {

  /**
   * 获取认证方案名称
   *
   * @return 方案名称，如 "satoken", "oauth2", "jwt"
   */
  String getName();

  /**
   * 执行登录
   *
   * @param loginUser 登录用户信息
   * @return Token 字符串
   */
  String login(LoginUser loginUser);

  /** 执行登出 */
  void logout();

  /**
   * 判断是否已登录
   *
   * @return true-已登录，false-未登录
   */
  boolean isLogin();

  /**
   * 获取当前登录用户
   *
   * @return 登录用户信息，未登录返回 null
   */
  LoginUser getLoginUser();

  /**
   * 获取当前 Token
   *
   * @return Token 字符串，未登录返回 null
   */
  String getToken();

  /**
   * 验证 Token 是否有效
   *
   * @param token Token 字符串
   * @return true-有效，false-无效
   */
  boolean validateToken(String token);

  /**
   * 刷新 Token
   *
   * @param timeout 新的过期时间（秒）
   */
  void refreshToken(long timeout);

  /**
   * 判断是否有权限
   *
   * @param permission 权限标识
   * @return true-有权限，false-无权限
   */
  boolean hasPermission(String permission);

  /**
   * 判断是否有角色
   *
   * @param role 角色标识
   * @return true-有角色，false-无角色
   */
  boolean hasRole(String role);
}
