package com.zhangzhankui.seed.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sa-Token 模式下的 Spring Security 配置
 *
 * <p>当 seed.auth.provider=satoken 或未配置时激活
 *
 * <p>禁用 Spring Security 的认证功能，由 Sa-Token 负责认证
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "seed.auth.provider", havingValue = "satoken", matchIfMissing = true)
public class SaTokenSecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        // 禁用 CSRF（API 服务无状态）
        .csrf(csrf -> csrf.disable())
        // 禁用 HTTP Basic 认证
        .httpBasic(httpBasic -> httpBasic.disable())
        // 禁用表单登录
        .formLogin(formLogin -> formLogin.disable())
        // 无状态会话
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // 允许所有请求通过 Spring Security（认证由 Sa-Token 处理）
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .build();
  }
}
