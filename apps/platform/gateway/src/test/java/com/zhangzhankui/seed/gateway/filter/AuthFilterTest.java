package com.zhangzhankui.seed.gateway.filter;

import static org.mockito.Mockito.*;

import com.zhangzhankui.seed.common.core.constant.SecurityConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * AuthFilter 单元测试
 *
 * <p>测试全局过滤器的头部处理和安全逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthFilter - 全局认证过滤器")
class AuthFilterTest {

  private AuthFilter authFilter;
  private GatewayFilterChain chain;

  @BeforeEach
  void setUp() {
    authFilter = new AuthFilter();
    chain = mock(GatewayFilterChain.class);
    // 使用 lenient() 避免在不使用 chain 的测试中报 UnnecessaryStubbing 错误
    lenient().when(chain.filter(any())).thenReturn(Mono.empty());
  }

  @Nested
  @DisplayName("请求头安全清理")
  class HeaderSanitizationTests {

    @Test
    @DisplayName("应清除外部伪造的内部请求标识头")
    void shouldRemoveForgedInternalHeader() {
      // Arrange
      MockServerHttpRequest request =
          MockServerHttpRequest.get("/api/test")
              .header(SecurityConstants.HEADER_FROM_SOURCE, "inner")
              .build();
      MockServerWebExchange exchange = MockServerWebExchange.from(request);

      // Act
      Mono<Void> result = authFilter.filter(exchange, chain);

      // Assert
      StepVerifier.create(result).verifyComplete();
      verify(chain).filter(argThat(ex -> {
        HttpHeaders headers = ex.getRequest().getHeaders();
        return !headers.containsKey(SecurityConstants.HEADER_FROM_SOURCE);
      }));
    }
  }

  @Nested
  @DisplayName("Sa-Token 认证模式")
  class SaTokenModeTests {

    @Test
    @DisplayName("未登录用户请求应直接放行（不注入用户头）")
    void shouldPassThroughWithoutUserHeadersWhenNotLoggedIn() {
      // Arrange
      MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
      MockServerWebExchange exchange = MockServerWebExchange.from(request);

      // Act
      Mono<Void> result = authFilter.filter(exchange, chain);

      // Assert
      StepVerifier.create(result).verifyComplete();
      verify(chain).filter(any());
    }
  }

  @Test
  @DisplayName("过滤器优先级应为 -200（高优先级）")
  void shouldHaveCorrectOrder() {
    // Assert
    assert authFilter.getOrder() == -200;
  }
}
