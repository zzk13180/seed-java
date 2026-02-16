package com.zhangzhankui.seed.common.rpc.config;

import com.zhangzhankui.seed.common.core.constant.SecurityConstants;
import com.zhangzhankui.seed.common.core.utils.ServletUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

/**
 * RPC 客户端自动配置
 *
 * <p>基于 Spring 6.1+ RestClient 和 HTTP Interface
 */
@AutoConfiguration
public class RpcAutoConfiguration {

  /**
   * 请求头透传拦截器
   *
   * <p>透传 Token、用户信息、租户信息等请求头
   */
  @Bean
  public ClientHttpRequestInterceptor headerPropagationInterceptor() {
    return (request, body, execution) -> {
      // 透传 Token
      String token = ServletUtils.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
      if (token != null && !token.isEmpty()) {
        request.getHeaders().add(SecurityConstants.AUTHORIZATION_HEADER, token);
      }

      // 透传用户信息
      String userId = ServletUtils.getHeader(SecurityConstants.HEADER_USER_ID);
      if (userId != null) {
        request.getHeaders().add(SecurityConstants.HEADER_USER_ID, userId);
      }

      String username = ServletUtils.getHeader(SecurityConstants.HEADER_USERNAME);
      if (username != null) {
        request.getHeaders().add(SecurityConstants.HEADER_USERNAME, username);
      }

      String tenantId = ServletUtils.getHeader(SecurityConstants.HEADER_TENANT_ID);
      if (tenantId != null) {
        request.getHeaders().add(SecurityConstants.HEADER_TENANT_ID, tenantId);
      }

      // 标记为内部请求
      request.getHeaders().add(SecurityConstants.HEADER_FROM_SOURCE, SecurityConstants.INNER);

      return execution.execute(request, body);
    };
  }

  /**
   * 负载均衡 RestClient.Builder
   *
   * <p>支持服务发现和负载均衡
   */
  @Bean
  @LoadBalanced
  public RestClient.Builder loadBalancedRestClientBuilder(
      ClientHttpRequestInterceptor headerPropagationInterceptor) {
    return RestClient.builder().requestInterceptor(headerPropagationInterceptor);
  }
}
