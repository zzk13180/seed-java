package com.zhangzhankui.seed.common.log.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 操作日志测试 */
@DisplayName("操作日志测试")
class OperLogTest {

  @Test
  @DisplayName("应实现Serializable接口")
  void shouldImplementSerializable() {
    OperLog operLog = new OperLog();
    assertThat(operLog).isInstanceOf(java.io.Serializable.class);
  }

  @Test
  @DisplayName("应能设置和获取日志字段")
  void shouldSetAndGetFields() {
    OperLog operLog = new OperLog();
    operLog.setTitle("测试操作");
    operLog.setBusinessType(1);
    operLog.setMethod("testMethod");
    operLog.setRequestMethod("POST");
    operLog.setOperUrl("/test");
    operLog.setOperIp("127.0.0.1");
    operLog.setOperLocation("本地");
    operLog.setOperParam("param");
    operLog.setJsonResult("result");
    operLog.setStatus(0);
    operLog.setErrorMsg("error");
    operLog.setOperTime(java.time.LocalDateTime.now());

    assertThat(operLog.getTitle()).isEqualTo("测试操作");
    assertThat(operLog.getBusinessType()).isEqualTo(1);
    assertThat(operLog.getMethod()).isEqualTo("testMethod");
    assertThat(operLog.getRequestMethod()).isEqualTo("POST");
    assertThat(operLog.getOperUrl()).isEqualTo("/test");
    assertThat(operLog.getOperIp()).isEqualTo("127.0.0.1");
    assertThat(operLog.getOperLocation()).isEqualTo("本地");
    assertThat(operLog.getOperParam()).isEqualTo("param");
    assertThat(operLog.getJsonResult()).isEqualTo("result");
    assertThat(operLog.getStatus()).isEqualTo(0);
    assertThat(operLog.getErrorMsg()).isEqualTo("error");
    assertThat(operLog.getOperTime()).isNotNull();
  }
}
