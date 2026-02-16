package com.zhangzhankui.seed.auth.config;

import com.zhangzhankui.seed.system.api.RemoteUserService;
import com.zhangzhankui.seed.system.api.dto.SysUserDTO;
import com.zhangzhankui.seed.common.core.constant.SecurityConstants;
import com.zhangzhankui.seed.common.core.constant.ServiceNameConstants;
import com.zhangzhankui.seed.common.core.domain.ApiResult;
import com.zhangzhankui.seed.common.core.domain.LoginUser;
import com.zhangzhankui.seed.common.security.inner.InnerAuthSigner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * HTTP 客户端配置
 *
 * <p>基于 Spring HTTP Interface 创建远程服务代理
 */
@Slf4j
@Configuration
public class HttpClientConfig {

  /**
   * 创建用户远程服务代理
   *
   * <p>使用负载均衡的 RestClient，支持服务发现。 每次请求动态生成 HMAC 签名，确保内部认证安全。
   */
  @Bean
  public RemoteUserService remoteUserService(
      RestClient.Builder loadBalancedRestClientBuilder, InnerAuthSigner innerAuthSigner) {
    RestClient restClient =
        loadBalancedRestClientBuilder
            .baseUrl("http://" + ServiceNameConstants.SYSTEM_SERVICE)
            .defaultHeader(SecurityConstants.HEADER_FROM_SOURCE, SecurityConstants.INNER)
            .requestInitializer(
                request -> {
                  // 每次请求都生成新的 HMAC 签名（含时间戳，防重放）
                  long timestamp = System.currentTimeMillis();
                  String signature = innerAuthSigner.sign(timestamp);
                  request.getHeaders().set(SecurityConstants.HEADER_INNER_AUTH_SIGN, signature);
                  request
                      .getHeaders()
                      .set(
                          SecurityConstants.HEADER_INNER_AUTH_TIMESTAMP,
                          String.valueOf(timestamp));
                })
            .build();

    HttpServiceProxyFactory factory =
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();

    // 创建带降级处理的代理
    RemoteUserService delegate = factory.createClient(RemoteUserService.class);
    return new RemoteUserServiceFallback(delegate);
  }

  /** 带降级处理的包装器 */
  private static class RemoteUserServiceFallback implements RemoteUserService {

    private final RemoteUserService delegate;

    RemoteUserServiceFallback(RemoteUserService delegate) {
      this.delegate = delegate;
    }

    @Override
    public ApiResult<LoginUser> getUserInfo(String username) {
      try {
        return delegate.getUserInfo(username);
      } catch (Exception e) {
        log.error("用户服务调用失败: {}", e.getMessage());
        return ApiResult.fail("获取用户信息失败: " + e.getMessage());
      }
    }

    @Override
    public ApiResult<LoginUser> getUserInfoById(Long userId) {
      try {
        return delegate.getUserInfoById(userId);
      } catch (Exception e) {
        log.error("用户服务调用失败: {}", e.getMessage());
        return ApiResult.fail("获取用户信息失败: " + e.getMessage());
      }
    }

    @Override
    public ApiResult<com.zhangzhankui.seed.system.api.dto.UserCredentialsDTO> getUserCredentials(
        String username) {
      try {
        return delegate.getUserCredentials(username);
      } catch (Exception e) {
        log.error("获取用户凭据失败: {}", e.getMessage());
        return ApiResult.fail("获取用户凭据失败: " + e.getMessage());
      }
    }

    @Override
    public ApiResult<Void> createUser(SysUserDTO dto) {
      try {
        return delegate.createUser(dto);
      } catch (Exception e) {
        log.error("创建用户失败: {}", e.getMessage());
        return ApiResult.fail("创建用户失败: " + e.getMessage());
      }
    }

    @Override
    public ApiResult<Void> createOAuth2User(SysUserDTO dto) {
      try {
        return delegate.createOAuth2User(dto);
      } catch (Exception e) {
        log.error("创建OAuth2用户失败: {}", e.getMessage());
        return ApiResult.fail("创建OAuth2用户失败: " + e.getMessage());
      }
    }
  }
}
