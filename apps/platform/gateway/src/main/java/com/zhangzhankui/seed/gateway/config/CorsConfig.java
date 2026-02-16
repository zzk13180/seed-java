package com.zhangzhankui.seed.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 网关 CORS 跨域配置
 *
 * <p>支持前端应用跨域访问 API
 * <p>默认允许所有 localhost 端口（开发环境友好），生产环境须通过
 * 环境变量 {@code CORS_ALLOWED_ORIGINS} 配置具体域名
 * <p>安全说明：生产环境禁止使用 * 作为允许来源
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:[*]}")
    private String allowedOrigins;

    private final Environment environment;

    @PostConstruct
    public void validateCorsConfig() {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        boolean isProduction = activeProfiles.contains("prod") || activeProfiles.contains("production");
        if (isProduction && allowedOrigins.contains("*")) {
            log.error("生产环境禁止使用通配符 '*' 作为 CORS allowed-origins，请通过环境变量配置具体域名");
            throw new IllegalStateException("CORS allowed-origins 不能在生产环境中使用 '*'");
        }
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 允许的来源（通过环境变量配置，生产环境应限制为具体域名）
        corsConfiguration.setAllowedOriginPatterns(
            Stream.of(allowedOrigins.split(","))
                .map(String::trim)
                .toList());

        // 允许的请求方法
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 允许的请求头
        corsConfiguration.setAllowedHeaders(List.of("*"));

        // 暴露的响应头（前端可以访问）
        corsConfiguration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Total-Count",
            "X-Request-Id"
        ));

        // 允许携带凭证（cookies、Authorization header）
        corsConfiguration.setAllowCredentials(true);

        // 预检请求缓存时间（秒）
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(source);
    }
}
