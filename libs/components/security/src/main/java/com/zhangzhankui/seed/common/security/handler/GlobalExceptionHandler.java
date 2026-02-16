package com.zhangzhankui.seed.common.security.handler;

import java.net.URI;
import java.util.stream.Collectors;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.zhangzhankui.seed.common.core.exception.InnerAuthException;
import com.zhangzhankui.seed.common.core.exception.ServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * <p>使用 RFC 9457 Problem Details 标准格式返回错误响应，同时支持 Sa-Token 和 Spring Security 异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final URI PROBLEM_TYPE_BASE = URI.create("https://api.zhangzhankui.com/problems/");

  /** 内部认证异常 - 统一使用 ProblemDetail 格式 */
  @ExceptionHandler(InnerAuthException.class)
  public ProblemDetail handleInnerAuthException(InnerAuthException e) {
    log.warn("内部认证失败: {}", e.getMessage());
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "内部认证失败");
    problem.setType(PROBLEM_TYPE_BASE.resolve("inner-auth-error"));
    problem.setTitle("内部认证失败");
    return problem;
  }

  /** 业务异常 */
  @ExceptionHandler(ServiceException.class)
  public ProblemDetail handleServiceException(ServiceException e) {
    log.warn("业务异常: {}", e.getMessage());
    HttpStatus status;
    try {
      status = HttpStatus.valueOf(e.getCode());
    } catch (IllegalArgumentException ex) {
      // 无效的 HTTP 状态码，回退到 BAD_REQUEST
      status = HttpStatus.BAD_REQUEST;
    }
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
    problem.setType(PROBLEM_TYPE_BASE.resolve("business-error"));
    problem.setTitle("业务异常");
    return problem;
  }

  /** 参数校验异常 - @Valid 校验失败 */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidException(MethodArgumentNotValidException e) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
    log.warn("参数校验失败: {}", message);
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
    problem.setType(PROBLEM_TYPE_BASE.resolve("validation-error"));
    problem.setTitle("参数校验失败");
    problem.setProperty(
        "errors",
        e.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ValidationError(fe.getField(), fe.getDefaultMessage()))
            .toList());
    return problem;
  }

  /** 约束违反异常 - @Validated 校验失败 */
  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraintViolation(ConstraintViolationException e) {
    String message =
        e.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
    log.warn("约束校验失败: {}", message);
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
    problem.setType(PROBLEM_TYPE_BASE.resolve("constraint-violation"));
    problem.setTitle("约束校验失败");
    return problem;
  }

  /** 请求体格式异常 */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
    log.warn("请求体格式错误: {}", e.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "请求体格式错误");
    problem.setType(PROBLEM_TYPE_BASE.resolve("malformed-request"));
    problem.setTitle("请求格式错误");
    return problem;
  }

  /** 未登录异常 */
  @ExceptionHandler(NotLoginException.class)
  public ProblemDetail handleNotLoginException(NotLoginException e) {
    log.warn("未登录: {}", e.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "请先登录");
    problem.setType(PROBLEM_TYPE_BASE.resolve("not-authenticated"));
    problem.setTitle("未认证");
    return problem;
  }

  /** 无权限异常 */
  @ExceptionHandler(NotPermissionException.class)
  public ProblemDetail handleNotPermissionException(NotPermissionException e) {
    log.warn("无权限: {}", e.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "无权限访问该资源");
    problem.setType(PROBLEM_TYPE_BASE.resolve("access-denied"));
    problem.setTitle("权限不足");
    return problem;
  }

  /** 无角色异常 - Sa-Token */
  @ExceptionHandler(NotRoleException.class)
  public ProblemDetail handleNotRoleException(NotRoleException e) {
    log.warn("无角色: {}", e.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "无权限访问该资源");
    problem.setType(PROBLEM_TYPE_BASE.resolve("access-denied"));
    problem.setTitle("角色不足");
    return problem;
  }

  // ========== Spring Security 异常（OAuth2 模式） ==========

  /** 认证异常 - Spring Security（未登录/Token无效） */
  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleAuthenticationException(AuthenticationException e) {
    log.warn("认证失败: {}", e.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "认证失败");
    problem.setType(PROBLEM_TYPE_BASE.resolve("authentication-failed"));
    problem.setTitle("认证失败");
    return problem;
  }

  /** 授权异常 - Spring Security（无权限） */
  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDeniedException(AccessDeniedException e) {
    log.warn("访问拒绝: {}", e.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "无权限访问该资源");
    problem.setType(PROBLEM_TYPE_BASE.resolve("access-denied"));
    problem.setTitle("访问被拒绝");
    return problem;
  }

  /** 运行时异常 */
  @ExceptionHandler(RuntimeException.class)
  public ProblemDetail handleRuntimeException(RuntimeException e) {
    log.error("运行时异常: ", e);
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "系统异常，请联系管理员");
    problem.setType(PROBLEM_TYPE_BASE.resolve("internal-error"));
    problem.setTitle("系统异常");
    return problem;
  }

  /** 系统异常 */
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleException(Exception e) {
    log.error("系统异常: ", e);
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "系统异常，请联系管理员");
    problem.setType(PROBLEM_TYPE_BASE.resolve("internal-error"));
    problem.setTitle("系统异常");
    return problem;
  }

  /** 校验错误详情 */
  public record ValidationError(String field, String message) {}
}
