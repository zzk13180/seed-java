package com.zhangzhankui.seed.common.security.provider;

import com.zhangzhankui.seed.common.core.auth.AuthProviderType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 认证配置
 *
 * <p>通过配置选择认证方案：
 *
 * <pre>
 * seed:
 *   auth:
 *     provider: satoken    # 或 oauth2
 *     oauth2:
 *       issuer-uri: https://your-logto.app/oidc
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "seed.auth")
public class AuthProperties {

  /**
   * 认证提供者类型
   *
   * <ul>
   *   <li>satoken - Sa-Token 自研认证（默认）
   *   <li>oauth2 - OAuth2/OIDC 外部认证（Logto、Keycloak 等）
   * </ul>
   */
  private AuthProviderType provider = AuthProviderType.SATOKEN;

  /** OAuth2 配置（provider=oauth2 时生效） */
  private OAuth2Config oauth2 = new OAuth2Config();

  /** OAuth2 配置 */
  @Data
  public static class OAuth2Config {

    /**
     * OIDC Provider 的 Issuer URI
     *
     * <p>例如：https://your-logto-instance.logto.app/oidc
     */
    private String issuerUri;

    /** OAuth2 Client ID */
    private String clientId;

    /** OAuth2 Client Secret */
    private String clientSecret;

    /** 前端回调地址 */
    private String frontendRedirectUri = "http://localhost:3000/auth/callback";

    /**
     * 从 JWT 中提取角色的 claim 名称
     *
     * <p>Logto 使用 "roles"，Keycloak 使用 "realm_access.roles"
     */
    private String rolesClaim = "roles";

    /** 角色前缀 */
    private String rolePrefix = "ROLE_";
  }

  /** 判断是否使用 Sa-Token */
  public boolean isSaToken() {
    return provider == AuthProviderType.SATOKEN;
  }

  /** 判断是否使用 OAuth2 */
  public boolean isOAuth2() {
    return provider == AuthProviderType.OAUTH2;
  }
}
