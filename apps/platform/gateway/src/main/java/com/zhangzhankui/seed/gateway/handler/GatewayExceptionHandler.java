package com.zhangzhankui.seed.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangzhankui.seed.common.core.domain.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** 网关统一异常处理 */
@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

  private final ObjectMapper objectMapper;

  @Override
  public @NonNull Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
    ServerHttpResponse response = exchange.getResponse();

    if (response.isCommitted()) {
      return Mono.error(ex);
    }

    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    ApiResult<Void> result;
    if (ex instanceof ResponseStatusException responseStatusException) {
      int statusCode = responseStatusException.getStatusCode().value();
      HttpStatus status = HttpStatus.resolve(statusCode);
      if (status != null) {
        response.setStatusCode(status);
        result = ApiResult.fail(status.value(), status.getReasonPhrase());
      } else {
        response.setStatusCode(HttpStatusCode.valueOf(statusCode));
        result = ApiResult.fail(statusCode, responseStatusException.getReason());
      }
    } else {
      log.error("网关异常: ", ex);
      response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
      result = ApiResult.fail("网关异常");
    }

    try {
      byte[] bytes = objectMapper.writeValueAsBytes(result);
      DataBuffer buffer = response.bufferFactory().wrap(bytes);
      return response.writeWith(Mono.just(buffer));
    } catch (JsonProcessingException e) {
      log.error("JSON序列化异常", e);
      return Mono.error(e);
    }
  }
}
