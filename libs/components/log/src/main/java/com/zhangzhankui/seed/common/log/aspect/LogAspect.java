package com.zhangzhankui.seed.common.log.aspect;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangzhankui.seed.common.core.annotation.Log;
import com.zhangzhankui.seed.common.core.utils.ServletUtils;
import com.zhangzhankui.seed.common.core.utils.SpringUtils;
import com.zhangzhankui.seed.common.log.event.OperLog;
import com.zhangzhankui.seed.common.log.event.OperLogEvent;
import com.zhangzhankui.seed.common.security.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/** 操作日志切面 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

  private final ObjectMapper objectMapper;

  /** 计算操作消耗时间 */
  private static final ThreadLocal<Long> TIME_THREADLOCAL = new ThreadLocal<>();

  @Before("@annotation(controllerLog)")
  public void doBefore(JoinPoint joinPoint, Log controllerLog) {
    TIME_THREADLOCAL.set(System.currentTimeMillis());
  }

  @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
  public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
    handleLog(joinPoint, controllerLog, null, jsonResult);
  }

  @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
  public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
    handleLog(joinPoint, controllerLog, e, null);
  }

  private void handleLog(
      final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
    try {
      OperLog operLog = new OperLog();
      operLog.setStatus(0);
      operLog.setOperTime(LocalDateTime.now());

      // 请求信息
      HttpServletRequest request = ServletUtils.getRequest();
      if (request != null) {
        operLog.setOperIp(ServletUtils.getClientIp());
        operLog.setOperUrl(truncate(request.getRequestURI(), 255));
        operLog.setRequestMethod(request.getMethod());
      }

      // 用户信息
      try {
        operLog.setOperName(SecurityUtils.getUsername());
        operLog.setTenantId(SecurityUtils.getTenantId());
      } catch (Exception ex) {
        log.debug("获取操作用户信息失败: {}", ex.getMessage());
      }

      // 异常信息
      if (e != null) {
        operLog.setStatus(1);
        operLog.setErrorMsg(truncate(e.getMessage(), 2000));
      }

      // 方法信息
      MethodSignature signature = (MethodSignature) joinPoint.getSignature();
      Method method = signature.getMethod();
      operLog.setMethod(method.getDeclaringClass().getName() + "." + method.getName() + "()");

      // 注解信息
      operLog.setTitle(controllerLog.title());
      operLog.setBusinessType(controllerLog.businessType().ordinal());
      operLog.setOperatorType(controllerLog.operatorType().ordinal());

      // 请求参数
      if (controllerLog.isSaveRequestData()) {
        setRequestValue(joinPoint, operLog);
      }

      // 响应结果
      if (controllerLog.isSaveResponseData() && jsonResult != null) {
        operLog.setJsonResult(truncate(objectMapper.writeValueAsString(jsonResult), 2000));
      }

      // 消耗时间
      Long startTime = TIME_THREADLOCAL.get();
      if (startTime != null) {
        operLog.setCostTime(System.currentTimeMillis() - startTime);
      }

      // 发布事件
      SpringUtils.getApplicationContext().publishEvent(new OperLogEvent(operLog));
    } catch (Exception ex) {
      log.error("记录操作日志异常", ex);
    } finally {
      TIME_THREADLOCAL.remove();
    }
  }

  private void setRequestValue(JoinPoint joinPoint, OperLog operLog) {
    try {
      Object[] args = joinPoint.getArgs();
      if (args != null && args.length > 0) {
        String params =
            Arrays.stream(args)
                .map(
                    arg -> {
                      try {
                        return objectMapper.writeValueAsString(arg);
                      } catch (Exception e) {
                        return arg.toString();
                      }
                    })
                .collect(Collectors.joining(","));
        operLog.setOperParam(truncate(params, 2000));
      }
    } catch (Exception ex) {
      log.debug("获取请求参数失败: {}", ex.getMessage());
    }
  }

  /** 截断字符串到指定长度 */
  private String truncate(String str, int maxLength) {
    if (str == null) {
      return null;
    }
    return str.length() <= maxLength ? str : str.substring(0, maxLength);
  }
}
