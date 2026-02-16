package com.zhangzhankui.seed.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * OAuth2 Client 安全配置
 *
 * <p>当 seed.auth.provider=oauth2 时激活
 *
 * <p>使用 Spring Security OAuth2 Client 处理 OIDC 登录流程
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "seed.auth.provider", havingValue = "oauth2")
public class OAuth2SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        // 启用 CSRF 防护（OAuth2 + Cookie 认证模式必须启用）
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .ignoringRequestMatchers("/actuator/**"))
        // 路由权限配置
        .authorizeHttpRequests(
            auth ->
                auth
                    // 健康检查、API 文档公开
                    .requestMatchers(
                        "/actuator/**",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**")
                    .permitAll()
                    // 其他接口需要认证
                    .anyRequest()
                    .authenticated())
        // OAuth2 登录配置
        .oauth2Login(
            oauth2 ->
                oauth2
                    // 登录成功后重定向到 /auth/callback
                    .defaultSuccessUrl("/auth/callback", true)
                    // 登录页面（自动重定向到 OIDC Provider）
                    .loginPage("/oauth2/authorization/logto"))
        // OAuth2 客户端配置
        .oauth2Client(oauth2 -> {})
        .build();
  }
}
