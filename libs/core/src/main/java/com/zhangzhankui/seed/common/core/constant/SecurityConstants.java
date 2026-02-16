package com.zhangzhankui.seed.common.core.constant;

/** 安全相关常量 */
public interface SecurityConstants {

  /** 用户ID请求头 */
  String HEADER_USER_ID = "X-User-Id";

  /** 用户名请求头 */
  String HEADER_USERNAME = "X-Username";

  /** 租户ID请求头 */
  String HEADER_TENANT_ID = "X-Tenant-Id";

  /** 请求来源 */
  String HEADER_FROM_SOURCE = "X-From-Source";

  /** 内部请求标识 */
  String INNER = "inner";

  /** 内部认证 HMAC 签名请求头 */
  String HEADER_INNER_AUTH_SIGN = "X-Inner-Auth-Sign";

  /** 内部认证时间戳请求头 */
  String HEADER_INNER_AUTH_TIMESTAMP = "X-Inner-Auth-Timestamp";

  /** HMAC 签名有效期（毫秒），默认 5 分钟 */
  long INNER_AUTH_SIGN_TTL_MS = 5 * 60 * 1000L;

  /** 用户标识 */
  String USER_KEY = "user_key";

  /** 登录用户 */
  String LOGIN_USER = "login_user";

  /** 授权信息 */
  String AUTHORIZATION_HEADER = "Authorization";

  /** Bearer 前缀 */
  String BEARER_PREFIX = "Bearer ";

  // ========== 角色常量 ==========

  /** 超级管理员角色标识 */
  String ROLE_ADMIN = "admin";

  /** 超级管理员拥有所有权限 */
  String ALL_PERMISSIONS = "*:*:*";
}
