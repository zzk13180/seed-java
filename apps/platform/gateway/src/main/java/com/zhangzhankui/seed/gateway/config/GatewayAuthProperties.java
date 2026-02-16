package com.zhangzhankui.seed.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** 认证方案配置属性 */
@Data
@Configuration
@ConfigurationProperties(prefix = "seed.auth")
public class GatewayAuthProperties {

  /** 认证方案：satoken (默认) 或 oauth2 */
  private String provider = "satoken";

  /** OAuth2 配置 */
  private OAuth2 oauth2 = new OAuth2();

  @Data
  public static class OAuth2 {
    /** OIDC Issuer URI */
    private String issuerUri;

    /** 角色对应的 Claim 名称 */
    private String rolesClaim = "roles";

    /** 角色前缀 */
    private String rolePrefix = "ROLE_";
  }
}
