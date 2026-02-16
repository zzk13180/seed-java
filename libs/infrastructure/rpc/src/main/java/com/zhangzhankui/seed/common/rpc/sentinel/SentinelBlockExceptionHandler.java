package com.zhangzhankui.seed.common.rpc.sentinel;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangzhankui.seed.common.core.domain.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Sentinel 限流异常处理器 */
@Component
@RequiredArgsConstructor
public class SentinelBlockExceptionHandler implements BlockExceptionHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      String resourceName,
      BlockException e)
      throws Exception {
    response.setContentType("application/json;charset=utf-8");
    response.setStatus(429);

    ApiResult<Void> result;
    if (e instanceof FlowException) {
      result = ApiResult.tooManyRequests();
    } else if (e instanceof DegradeException) {
      result = ApiResult.serviceUnavailable();
    } else if (e instanceof ParamFlowException) {
      result = ApiResult.fail(ApiResult.TOO_MANY_REQUESTS, "热点参数限流");
    } else if (e instanceof AuthorityException) {
      result = ApiResult.forbidden("授权规则不通过");
    } else {
      result = ApiResult.fail("系统繁忙，请稍后重试");
    }

    objectMapper.writeValue(response.getWriter(), result);
  }
}
