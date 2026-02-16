package com.zhangzhankui.seed.common.log.listener;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;

import com.zhangzhankui.seed.common.log.event.OperLog;
import com.zhangzhankui.seed.common.log.event.OperLogEvent;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/** 操作日志监听器测试 - 验证事件处理行为 */
@DisplayName("操作日志监听器测试")
class OperLogListenerTest {

  private final OperLogListener listener = new OperLogListener();

  @Test
  @DisplayName("handleOperLog 应能正常处理事件而不抛异常")
  void handleOperLogShouldProcessEventWithoutException() {
    OperLog operLog = new OperLog();
    operLog.setTitle("测试模块");
    operLog.setBusinessType(0);
    operLog.setMethod("com.test.TestController.test()");
    operLog.setCostTime(100L);
    operLog.setOperTime(LocalDateTime.now());

    OperLogEvent event = new OperLogEvent(operLog);

    assertThatNoException().isThrownBy(() -> listener.handleOperLog(event));
  }

  @Test
  @DisplayName("handleOperLog 应能处理字段为 null 的事件")
  void handleOperLogShouldHandleNullFields() {
    OperLog operLog = new OperLog();
    // All fields null except what's required
    OperLogEvent event = new OperLogEvent(operLog);

    assertThatNoException().isThrownBy(() -> listener.handleOperLog(event));
  }

  @Test
  @DisplayName("handleOperLog 方法应有 @EventListener 注解")
  void handleOperLogShouldHaveEventListenerAnnotation() throws NoSuchMethodException {
    var method = OperLogListener.class.getMethod("handleOperLog", OperLogEvent.class);
    assertThat(method.isAnnotationPresent(EventListener.class));
  }

  @Test
  @DisplayName("handleOperLog 方法应有 @Async 注解")
  void handleOperLogShouldHaveAsyncAnnotation() throws NoSuchMethodException {
    var method = OperLogListener.class.getMethod("handleOperLog", OperLogEvent.class);
    Async asyncAnnotation = method.getAnnotation(Async.class);
    org.assertj.core.api.Assertions.assertThat(asyncAnnotation).isNotNull();
    org.assertj.core.api.Assertions.assertThat(asyncAnnotation.value())
        .isEqualTo("asyncLogExecutor");
  }

  // Static import helper
  private static void assertThat(boolean condition) {
    org.assertj.core.api.Assertions.assertThat(condition).isTrue();
  }
}
