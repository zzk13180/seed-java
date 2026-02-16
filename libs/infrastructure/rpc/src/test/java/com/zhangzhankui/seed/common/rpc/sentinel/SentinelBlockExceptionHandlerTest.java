package com.zhangzhankui.seed.common.rpc.sentinel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangzhankui.seed.common.core.domain.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

/** Sentinel异常处理器测试 - 验证 handle() 写入正确的 HTTP 响应 */
@DisplayName("Sentinel异常处理器测试")
class SentinelBlockExceptionHandlerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private SentinelBlockExceptionHandler handler;
  private HttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    handler = new SentinelBlockExceptionHandler(objectMapper);
    request = mock(HttpServletRequest.class);
    response = new MockHttpServletResponse();
  }

  @Test
  @DisplayName("FlowException 应返回 429 限流响应")
  void shouldReturn429ForFlowException() throws Exception {
    FlowException ex = new FlowException("flow");

    handler.handle(request, response, "resource", ex);

    assertThat(response.getStatus()).isEqualTo(429);
    assertThat(response.getContentType()).isEqualTo("application/json;charset=utf-8");

    JsonNode json = objectMapper.readTree(response.getContentAsString());
    assertThat(json.get("code").asInt()).isEqualTo(ApiResult.TOO_MANY_REQUESTS);
  }

  @Test
  @DisplayName("DegradeException 应返回 429 服务不可用响应")
  void shouldReturn429ForDegradeException() throws Exception {
    DegradeException ex = new DegradeException("degrade");

    handler.handle(request, response, "resource", ex);

    assertThat(response.getStatus()).isEqualTo(429);
    JsonNode json = objectMapper.readTree(response.getContentAsString());
    assertThat(json.get("code").asInt()).isEqualTo(ApiResult.SERVICE_UNAVAILABLE);
  }

  @Test
  @DisplayName("ParamFlowException 应返回 429 热点参数限流响应")
  void shouldReturn429ForParamFlowException() throws Exception {
    ParamFlowException ex = new ParamFlowException("resource", "param");

    handler.handle(request, response, "resource", ex);

    assertThat(response.getStatus()).isEqualTo(429);
    JsonNode json = objectMapper.readTree(response.getContentAsString());
    assertThat(json.get("code").asInt()).isEqualTo(ApiResult.TOO_MANY_REQUESTS);
    assertThat(json.get("message").asText()).isEqualTo("热点参数限流");
  }

  @Test
  @DisplayName("AuthorityException 应返回 429 授权不通过响应")
  void shouldReturn429ForAuthorityException() throws Exception {
    AuthorityException ex = new AuthorityException("authority");

    handler.handle(request, response, "resource", ex);

    assertThat(response.getStatus()).isEqualTo(429);
    JsonNode json = objectMapper.readTree(response.getContentAsString());
    assertThat(json.get("code").asInt()).isEqualTo(ApiResult.FORBIDDEN);
    assertThat(json.get("message").asText()).isEqualTo("授权规则不通过");
  }

  @Test
  @DisplayName("响应 Content-Type 应为 application/json;charset=utf-8")
  void shouldSetCorrectContentType() throws Exception {
    FlowException ex = new FlowException("flow");

    handler.handle(request, response, "resource", ex);

    assertThat(response.getContentType()).isEqualTo("application/json;charset=utf-8");
  }
}
