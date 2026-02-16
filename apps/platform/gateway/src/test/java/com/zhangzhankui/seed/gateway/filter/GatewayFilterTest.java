package com.zhangzhankui.seed.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.zhangzhankui.seed.common.core.constant.SecurityConstants;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * 网关过滤器单元测试
 *
 * <p>通过实例化真实的 {@link AuthFilter}，构造 {@link MockServerWebExchange}，调用 {@code
 * filter(exchange, chain)} 并验证下游收到的请求头和 chain 调用情况。
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("网关过滤器测试")
class GatewayFilterTest {

  private AuthFilter authFilter;
  private GatewayFilterChain chain;

  /** chain.filter 被调用时捕获的 exchange，用于验证过滤器对请求的修改 */
  private ServerWebExchange capturedExchange;

  @BeforeEach
  void setUp() throws Exception {
    authFilter = new AuthFilter();
    setAuthProvider("satoken");

    chain = mock(GatewayFilterChain.class);
    lenient()
        .when(chain.filter(any()))
        .thenAnswer(
            invocation -> {
              capturedExchange = invocation.getArgument(0);
              return Mono.empty();
            });
  }

  /**
   * 通过反射设置 {@code authProvider} 字段（该字段由 {@code @Value} 注入，单元测试中手动赋值）。
   */
  private void setAuthProvider(String provider) throws Exception {
    Field field = AuthFilter.class.getDeclaredField("authProvider");
    field.setAccessible(true);
    field.set(authFilter, provider);
  }

  // ========== 请求头安全清理 ==========

  @Nested
  @DisplayName("请求头安全清理")
  class HeaderSanitizationTests {

    @Test
    @DisplayName("应清除外部伪造的内部请求标识头 X-From-Source")
    void shouldRemoveForgedFromSourceHeader() {
      MockServerHttpRequest request =
          MockServerHttpRequest.get("/api/test")
              .header(SecurityConstants.HEADER_FROM_SOURCE, "inner")
              .build();
      MockServerWebExchange exchange = MockServerWebExchange.from(request);

      StepVerifier.create(authFilter.filter(exchange, chain)).verifyComplete();

      assertThat(capturedExchange).isNotNull();
      HttpHeaders headers = capturedExchange.getRequest().getHeaders();
      assertThat(headers.containsKey(SecurityConstants.HEADER_FROM_SOURCE)).isFalse();
    }

    @Test
    @DisplayName("无 X-From-Source 头的请求应正常通过")
    void shouldPassRequestWithoutFromSourceHeader() {
      MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
      MockServerWebExchange exchange = MockServerWebExchange.from(request);

      StepVerifier.create(authFilter.filter(exchange, chain)).verifyComplete();

      verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("应保留非敏感请求头（只清除 X-From-Source）")
    void shouldPreserveNonSensitiveHeaders() {
      MockServerHttpRequest request =
          MockServerHttpRequest.get("/api/test")
              .header(SecurityConstants.HEADER_FROM_SOURCE, "inner")
              .header("Authorization", "Bearer some-token")
              .header("X-Custom", "value")
              .header("Accept", "application/json")
              .build();
      MockServerWebExchange exchange = MockServerWebExchange.from(request);

      StepVerifier.create(authFilter.filter(exchange, chain)).verifyComplete();

      HttpHeaders headers = capturedExchange.getRequest().getHeaders();
      assertThat(headers.containsKey(SecurityConstants.HEADER_FROM_SOURCE)).isFalse();
      assertThat(headers.getFirst("Authorization")).isEqualTo("Bearer some-token");
      assertThat(headers.getFirst("X-Custom")).isEqualTo("value");
      assertThat(headers.getFirst("Accept")).isEqualTo("application/json");
    }

    @Test
    @DisplayName("X-From-Source 有多个值时应全部清除")
    void shouldRemoveAllFromSourceValues() {
      MockServerHttpRequest request =
          MockServerHttpRequest.get("/api/test")
              .header(SecurityConstants.HEADER_FROM_SOURCE, "inner", "extra")
              .build();
      MockServerWebExchange exchange = MockServerWebExchange.from(request);

      StepVerifier.create(authFilter.filter(exchange, chain)).verifyComplete();

      HttpHeaders headers = capturedExchange.getRequest().getHeaders();
      assertThat(headers.get(SecurityConstants.HEADER_FROM_SOURCE)).isNull();
    }
  }

  // ========== Sa-Token 模式（默认） ==========

  @Nested
  @DisplayName("Sa-Token 模式 - 未登录状态")
  class SaTokenUnauthorizedTests {

    @Test
    @DisplayName("未登录用户请求应被转发，且不注入用户信息头")
    void shouldForwardWithoutUserHeaders() {
      MockServerHttpRequest request = MockServerHttpRequest.get("/system/user/list").build();
      MockServerWebExchange exchange = MockServerWebExchange.from(request);

      StepVerifier.create(authFilter.filter(exchange, chain)).verifyComplete();

      HttpHeaders headers = capturedExchange.getRequest().getHeaders();
      assertThat(headers.containsKey(SecurityConstants.HEADER_USER_ID)).isFalse();
      assertThat(headers.containsKey(SecurityConstants.HEADER_USERNAME)).isFalse();
    }

    @Test
    @DisplayName("未登录状态下 chain.filter 仍然被调用（过滤器不拦截请求）")
    void shouldAlwaysCallChainFilter() {
      MockServerHttpRequest request = MockServerHttpRequest.get("/any/path").build();
      MockServerWebExchange exchange = MockServerWebExchange.from(request);

      StepVerifier.create(authFilter.filter(exchange, chain)).verifyComplete();

      verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("白名单路径请求也应经过 AuthFilter 处理（清除伪造头）")
    void shouldProcessWhitelistedPathsThroughFilter() {
      MockServerHttpRequest request =
          MockServerHttpRequest.get("/auth/login")
              .header(SecurityConstants.HEADER_FROM_SOURCE, "forged")
              .build();
      MockServerWebExchange exchange = MockServerWebExchange.from(request);

      StepVerifier.create(authFilter.filter(exchange, chain)).verifyComplete();

      verify(chain).filter(any(ServerWebExchange.class));
      HttpHeaders headers = capturedExchange.getRequest().getHeaders();
      assertThat(headers.containsKey(SecurityConstants.HEADER_FROM_SOURCE)).isFalse();
    }

    @Test
    @DisplayName("不同路径的请求均应被正常转发")
    void shouldForwardVariousPaths() {
      String[] paths = {"/system/user/list", "/auth/login", "/actuator/health", "/unknown"};
      for (String path : paths) {
        capturedExchange = null;
        reset(chain);
        when(chain.filter(any()))
            .thenAnswer(
                inv -> {
                  capturedExchange = inv.getArgument(0);
                  return Mono.empty();
                });

        MockServerHttpRequest request = MockServerHttpRequest.get(path).build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(authFilter.filter(exchange, chain)).verifyComplete();
        verify(chain).filter(any(ServerWebExchange.class));
      }
    }
  }

  // ========== OAuth2 模式 ==========

  @Nested
  @DisplayName("OAuth2 模式 - 无安全上下文")
  class OAuth2NoContextTests {

    @BeforeEach
    void setOAuth2Mode() throws Exception {
      setAuthProvider("oauth2");
    }

    @Test
    @DisplayName("无安全上下文时请求应正常转发")
    void shouldForwardWithoutSecurityContext() {
      MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
      MockServerWebExchange exchange = MockServerWebExchange.from(request);

      StepVerifier.create(authFilter.filter(exchange, chain)).verifyComplete();

      verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    @DisplayName("OAuth2 模式下也应清除 X-From-Source 头")
    void shouldRemoveFromSourceInOAuth2Mode() {
      MockServerHttpRequest request =
          MockServerHttpRequest.get("/api/test")
              .header(SecurityConstants.HEADER_FROM_SOURCE, "inner")
              .build();
      MockServerWebExchange exchange = MockServerWebExchange.from(request);

      StepVerifier.create(authFilter.filter(exchange, chain)).verifyComplete();

      HttpHeaders headers = capturedExchange.getRequest().getHeaders();
      assertThat(headers.containsKey(SecurityConstants.HEADER_FROM_SOURCE)).isFalse();
    }

    @Test
    @DisplayName("OAuth2 模式下未认证请求不应注入任何用户信息头")
    void shouldNotInjectUserHeadersWithoutAuth() {
      MockServerHttpRequest request = MockServerHttpRequest.get("/system/user/list").build();
      MockServerWebExchange exchange = MockServerWebExchange.from(request);

      StepVerifier.create(authFilter.filter(exchange, chain)).verifyComplete();

      HttpHeaders headers = capturedExchange.getRequest().getHeaders();
      assertThat(headers.containsKey(SecurityConstants.HEADER_USER_ID)).isFalse();
      assertThat(headers.containsKey(SecurityConstants.HEADER_USERNAME)).isFalse();
      assertThat(headers.containsKey(SecurityConstants.HEADER_TENANT_ID)).isFalse();
    }
  }

  // ========== 过滤器元数据 ==========

  @Nested
  @DisplayName("过滤器元数据")
  class FilterMetadataTests {

    @Test
    @DisplayName("过滤器优先级应为 -200（高优先级）")
    void shouldHaveHighPriority() {
      assertThat(authFilter.getOrder()).isEqualTo(-200);
    }

    @Test
    @DisplayName("过滤器应实现 GlobalFilter 接口")
    void shouldImplementGlobalFilter() {
      assertThat(authFilter)
          .isInstanceOf(org.springframework.cloud.gateway.filter.GlobalFilter.class);
    }

    @Test
    @DisplayName("过滤器应实现 Ordered 接口")
    void shouldImplementOrdered() {
      assertThat(authFilter).isInstanceOf(org.springframework.core.Ordered.class);
    }
  }
}
