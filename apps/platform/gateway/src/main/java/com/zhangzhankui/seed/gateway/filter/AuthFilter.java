package com.zhangzhankui.seed.gateway.filter;

import cn.dev33.satoken.stp.StpUtil;
import com.zhangzhankui.seed.common.core.constant.SecurityConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局过滤器 - 请求头处理
 *
 * <p>支持多认证模式：
 *
 * <ul>
 *   <li>Sa-Token 模式 - 从 Sa-Token Session 获取用户信息
 *   <li>OAuth2 模式 - 从 JWT Token 获取用户信息
 * </ul>
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

  @Value("${seed.auth.provider:satoken}")
  private String authProvider;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest.Builder mutate = exchange.getRequest().mutate();

    // 清除外部伪造的内部请求标识和签名头
    mutate.headers(h -> {
      h.remove(SecurityConstants.HEADER_FROM_SOURCE);
      h.remove(SecurityConstants.HEADER_INNER_AUTH_SIGN);
      h.remove(SecurityConstants.HEADER_INNER_AUTH_TIMESTAMP);
    });

    if ("oauth2".equalsIgnoreCase(authProvider)) {
      // OAuth2 模式：从 JWT 提取用户信息
      return extractUserInfoFromJwt(exchange, chain, mutate);
    } else {
      // Sa-Token 模式：从 Session 提取用户信息
      return extractUserInfoFromSaToken(exchange, chain, mutate);
    }
  }

  /** Sa-Token 模式：从 Session 提取用户信息传递给下游服务 */
  private Mono<Void> extractUserInfoFromSaToken(
      ServerWebExchange exchange, GatewayFilterChain chain, ServerHttpRequest.Builder mutate) {
    try {
      if (StpUtil.isLogin()) {
        Long userId = StpUtil.getLoginIdAsLong();
        mutate.header(SecurityConstants.HEADER_USER_ID, String.valueOf(userId));
        // 从 Session 中读取 LOGIN_USER 对象并提取 username
        Object loginUserObj = StpUtil.getSession().get(SecurityConstants.LOGIN_USER);
        if (loginUserObj instanceof com.zhangzhankui.seed.common.core.domain.LoginUser loginUser) {
          String username = loginUser.getUsername();
          if (username != null) {
            mutate.header(SecurityConstants.HEADER_USERNAME, username);
          }
          log.debug("Sa-Token 模式：传递用户信息 userId={}, username={}", userId, username);
        } else {
          log.debug("Sa-Token 模式：传递用户信息 userId={}, loginUser 未找到", userId);
        }
      }
    } catch (Exception e) {
      log.debug("Sa-Token 获取用户信息失败: {}", e.getMessage());
    }
    return chain.filter(exchange.mutate().request(mutate.build()).build());
  }

  /** OAuth2 模式：从 JWT 提取用户信息传递给下游服务 */
  private Mono<Void> extractUserInfoFromJwt(
      ServerWebExchange exchange, GatewayFilterChain chain, ServerHttpRequest.Builder mutate) {
    return ReactiveSecurityContextHolder.getContext()
        .map(ctx -> ctx.getAuthentication())
        .filter(auth -> auth instanceof JwtAuthenticationToken)
        .cast(JwtAuthenticationToken.class)
        .map(
            auth -> {
              Jwt jwt = auth.getToken();

              // 从 JWT 提取用户信息传递给下游服务
              String userId = jwt.getSubject();
              String username = getClaimAsString(jwt, "preferred_username", "username");
              String tenantId = jwt.getClaimAsString("tenant_id");

              if (userId != null) {
                mutate.header(SecurityConstants.HEADER_USER_ID, userId);
              }
              if (username != null) {
                mutate.header(SecurityConstants.HEADER_USERNAME, username);
              }
              if (tenantId != null) {
                mutate.header(SecurityConstants.HEADER_TENANT_ID, tenantId);
              }

              log.debug("OAuth2 模式：传递用户信息 userId={}, username={}", userId, username);

              return exchange.mutate().request(mutate.build()).build();
            })
        .defaultIfEmpty(exchange.mutate().request(mutate.build()).build())
        .flatMap(chain::filter);
  }

  /** 从 JWT 获取 claim，支持多个候选名称 */
  private String getClaimAsString(Jwt jwt, String... claimNames) {
    for (String name : claimNames) {
      String value = jwt.getClaimAsString(name);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  @Override
  public int getOrder() {
    return -200;
  }
}
