package com.zhangzhankui.seed.common.core.auth;

/** 认证提供者类型枚举 */
public enum AuthProviderType {

  /**
   * Sa-Token 认证
   *
   * <p>自研 Token 管理，支持分布式 Session
   */
  SATOKEN("satoken", "Sa-Token 认证"),

  /**
   * OAuth2/OIDC 认证
   *
   * <p>集成外部 OIDC Provider（Logto、Keycloak、Auth0 等）
   */
  OAUTH2("oauth2", "OAuth2/OIDC 认证"),

  /**
   * 简单 JWT 认证
   *
   * <p>自签发 JWT，无需外部服务
   */
  JWT("jwt", "JWT 认证");

  private final String code;
  private final String description;

  AuthProviderType(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  /** 根据 code 获取枚举 */
  public static AuthProviderType fromCode(String code) {
    for (AuthProviderType type : values()) {
      if (type.code.equalsIgnoreCase(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown auth provider type: " + code);
  }
}
