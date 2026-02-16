package com.zhangzhankui.seed.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangzhankui.seed.common.core.domain.ApiResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 认证配置（网关）
 *
 * <p>当 seed.auth.provider=satoken 或未配置时激活
 */
@Configuration
@ConditionalOnProperty(name = "seed.auth.provider", havingValue = "satoken", matchIfMissing = true)
public class SaTokenConfig {

  private final ObjectMapper objectMapper;

  public SaTokenConfig(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Bean
  public SaReactorFilter saReactorFilter() {
    return new SaReactorFilter()
        // 拦截所有路由
        .addInclude("/**")
        // 开放登录接口
        .addExclude("/auth/login", "/auth/register", "/auth/callback", "/auth/exchange")
        // 开放公共接口
        .addExclude(
            "/actuator/**", "/doc.html", "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**")
        // 鉴权方法
        .setAuth(
            obj -> {
              // 登录校验
              SaRouter.match("/**").notMatch("/auth/**").check(r -> StpUtil.checkLogin());
            })
        // 异常处理 - 使用 ObjectMapper 安全序列化
        .setError(
            e -> {
              try {
                return objectMapper.writeValueAsString(ApiResult.unauthorized());
              } catch (JsonProcessingException ex) {
                return "{\"code\":500,\"message\":\"系统异常\",\"data\":null}";
              }
            });
  }
}
