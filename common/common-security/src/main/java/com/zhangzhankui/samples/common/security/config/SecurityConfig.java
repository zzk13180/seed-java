package com.zhangzhankui.samples.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangzhankui.samples.common.core.controller.ResponseMessage;
import com.zhangzhankui.samples.common.core.enums.ResponseEnum;
import com.zhangzhankui.samples.common.security.filter.JwtAuthenticationFilter;
import com.zhangzhankui.samples.common.security.util.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    public SecurityConfig(SecurityProperties securityProperties, JwtUtils jwtUtils, ObjectMapper objectMapper) {
        this.securityProperties = securityProperties;
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
    }

    /**
     * 默认放行的路径
     */
    private static final String[] DEFAULT_PERMIT_PATHS = {
            // Swagger/OpenAPI
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/webjars/**",
            // Actuator
            "/actuator/**",
            // Static resources
            "/static/**",
            "/public/**",
            "/favicon.ico",
            // Error
            "/error",
            // Auth
            "/auth/login",
            "/auth/register",
            "/auth/captcha",
            // Health check
            "/health",
            "/"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UserDetailsService userDetailsService) throws Exception {
        
        // 构建放行路径
        String[] permitPaths = mergePermitPaths();
        
        http
            // 禁用CSRF (使用JWT无需CSRF)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 禁用Session (无状态)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置异常处理
            .exceptionHandling(exception -> exception
                // 未认证处理
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    PrintWriter writer = response.getWriter();
                    writer.write(objectMapper.writeValueAsString(
                            ResponseMessage.failed(ResponseEnum.UNAUTHORIZED)));
                    writer.flush();
                })
                // 无权限处理
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    PrintWriter writer = response.getWriter();
                    writer.write(objectMapper.writeValueAsString(
                            ResponseMessage.failed(ResponseEnum.FORBIDDEN)));
                    writer.flush();
                }))
            
            // 配置路径权限
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(permitPaths).permitAll()
                .anyRequest().authenticated())
            
            // 添加JWT过滤器
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtUtils, userDetailsService, securityProperties),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 合并放行路径
     */
    private String[] mergePermitPaths() {
        if (securityProperties.getIgnorePaths() == null || 
                securityProperties.getIgnorePaths().isEmpty()) {
            return DEFAULT_PERMIT_PATHS;
        }
        
        String[] customPaths = securityProperties.getIgnorePaths().toArray(new String[0]);
        String[] allPaths = new String[DEFAULT_PERMIT_PATHS.length + customPaths.length];
        System.arraycopy(DEFAULT_PERMIT_PATHS, 0, allPaths, 0, DEFAULT_PERMIT_PATHS.length);
        System.arraycopy(customPaths, 0, allPaths, DEFAULT_PERMIT_PATHS.length, customPaths.length);
        
        return allPaths;
    }
}
