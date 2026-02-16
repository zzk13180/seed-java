package com.zhangzhankui.seed.common.log.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangzhankui.seed.common.core.annotation.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/** 日志切面测试 - 验证 @Before 设置 ThreadLocal 计时 */
@ExtendWith(MockitoExtension.class)
@DisplayName("日志切面测试")
class LogAspectTest {

  @Spy private ObjectMapper objectMapper = new ObjectMapper();

  @InjectMocks private LogAspect logAspect;

  @Test
  @DisplayName("doBefore 应记录开始时间（不抛异常）")
  void doBeforeShouldRecordStartTime() {
    JoinPoint joinPoint = mock(JoinPoint.class);
    Log controllerLog = mock(Log.class);

    // Should not throw - it simply sets a ThreadLocal
    logAspect.doBefore(joinPoint, controllerLog);
  }

  @Test
  @DisplayName("类应有 @Aspect 注解")
  void classShouldHaveAspectAnnotation() {
    assertThat(LogAspect.class.isAnnotationPresent(org.aspectj.lang.annotation.Aspect.class))
        .isTrue();
  }

  @Test
  @DisplayName("doBefore 方法应有 @Before 注解")
  void doBeforeShouldHaveBeforeAnnotation() throws NoSuchMethodException {
    var method = LogAspect.class.getMethod("doBefore", JoinPoint.class, Log.class);
    assertThat(method.isAnnotationPresent(org.aspectj.lang.annotation.Before.class)).isTrue();
  }

  @Test
  @DisplayName("doAfterReturning 方法应有 @AfterReturning 注解")
  void doAfterReturningShouldHaveAfterReturningAnnotation() throws NoSuchMethodException {
    var method =
        LogAspect.class.getMethod("doAfterReturning", JoinPoint.class, Log.class, Object.class);
    assertThat(method.isAnnotationPresent(org.aspectj.lang.annotation.AfterReturning.class))
        .isTrue();
  }

  @Test
  @DisplayName("doAfterThrowing 方法应有 @AfterThrowing 注解")
  void doAfterThrowingShouldHaveAfterThrowingAnnotation() throws NoSuchMethodException {
    var method =
        LogAspect.class.getMethod("doAfterThrowing", JoinPoint.class, Log.class, Exception.class);
    assertThat(method.isAnnotationPresent(org.aspectj.lang.annotation.AfterThrowing.class))
        .isTrue();
  }
}
