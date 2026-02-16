package com.zhangzhankui.seed.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Sa-Token 模式下的 Spring Security 配置
 *
 * <p>当 seed.auth.provider=satoken 或未配置时激活
 *
 * <p>禁用 Spring Security 的认证功能，由 Sa-Token 负责认证
 */
@Configuration
@EnableWebFluxSecurity
@ConditionalOnProperty(name = "seed.auth.provider", havingValue = "satoken", matchIfMissing = true)
public class SaTokenSecurityConfig {

  @Bean
  public SecurityWebFilterChain saTokenSecurityWebFilterChain(ServerHttpSecurity http) {
    return http
        // 禁用 CSRF（API 网关无状态）
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        // 禁用 HTTP Basic 认证
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        // 禁用表单登录
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        // 允许所有请求通过 Spring Security（认证由 Sa-Token 处理）
        .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
        .build();
  }
}
