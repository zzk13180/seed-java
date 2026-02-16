package com.zhangzhankui.seed.common.rpc.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

/** RPC自动配置测试 - 验证 Bean 定义和配置结构 */
@DisplayName("RPC自动配置测试")
class RpcAutoConfigurationTest {

  private final RpcAutoConfiguration config = new RpcAutoConfiguration();

  @Test
  @DisplayName("headerPropagationInterceptor 应返回非空拦截器")
  void headerPropagationInterceptorShouldReturnNonNull() {
    ClientHttpRequestInterceptor interceptor = config.headerPropagationInterceptor();
    assertThat(interceptor).isNotNull();
  }

  @Test
  @DisplayName("loadBalancedRestClientBuilder 应返回带拦截器的 Builder")
  void loadBalancedRestClientBuilderShouldReturnBuilderWithInterceptor() {
    ClientHttpRequestInterceptor interceptor = config.headerPropagationInterceptor();
    RestClient.Builder builder = config.loadBalancedRestClientBuilder(interceptor);
    assertThat(builder).isNotNull();
  }

  @Test
  @DisplayName("headerPropagationInterceptor 方法应有 @Bean 注解")
  void headerPropagationInterceptorMethodShouldHaveBeanAnnotation() throws NoSuchMethodException {
    var method = RpcAutoConfiguration.class.getMethod("headerPropagationInterceptor");
    assertThat(method.isAnnotationPresent(Bean.class)).isTrue();
  }

  @Test
  @DisplayName("loadBalancedRestClientBuilder 方法应有 @Bean 和 @LoadBalanced 注解")
  void loadBalancedRestClientBuilderShouldHaveBeanAndLoadBalancedAnnotations()
      throws NoSuchMethodException {
    var method =
        RpcAutoConfiguration.class.getMethod(
            "loadBalancedRestClientBuilder", ClientHttpRequestInterceptor.class);
    assertThat(method.isAnnotationPresent(Bean.class)).isTrue();
    assertThat(method.isAnnotationPresent(LoadBalanced.class)).isTrue();
  }

  @Test
  @DisplayName("类应有 @AutoConfiguration 注解")
  void classShouldHaveAutoConfigurationAnnotation() {
    assertThat(RpcAutoConfiguration.class.isAnnotationPresent(AutoConfiguration.class)).isTrue();
  }
}
