package com.zhangzhankui.seed.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

/**
 * OAuth2 认证配置（网关）
 *
 * <p>当 seed.auth.provider=oauth2 时激活
 *
 * <p>使用 Spring Security OAuth2 Resource Server 验证 JWT Token
 */
@Configuration
@EnableWebFluxSecurity
@ConditionalOnProperty(name = "seed.auth.provider", havingValue = "oauth2")
public class OAuth2SecurityConfig {

  @Bean
  public SecurityWebFilterChain oauth2SecurityWebFilterChain(ServerHttpSecurity http) {
    return http
        // 禁用 CSRF（API 网关无状态）
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        // 路由权限配置
        .authorizeExchange(
            exchanges ->
                exchanges
                    // 公开接口 - 登录回调、健康检查、API文档
                    .pathMatchers(
                        "/auth/callback",
                        "/auth/login",
                        "/auth/logout",
                        "/auth/register",
                        "/auth/exchange",
                        "/actuator/**",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**")
                    .permitAll()
                    // 其他接口需要认证
                    .anyExchange()
                    .authenticated())
        // OAuth2 Resource Server 配置 - 使用 JWT
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .build();
  }

  /**
   * JWT 转换器 - 从 JWT 提取权限
   *
   * <p>可配置不同 OIDC Provider 的 roles claim：
   *
   * <ul>
   *   <li>Logto: "roles"
   *   <li>Keycloak: "realm_access.roles"
   *   <li>Auth0: "permissions"
   * </ul>
   */
  private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();
    // 默认使用 "roles" claim（Logto 格式）
    grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
    grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

    return new ReactiveJwtAuthenticationConverterAdapter(jwtConverter);
  }
}
