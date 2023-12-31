package com.zhangzhankui.samples.common.web.exception;

import com.zhangzhankui.samples.common.core.controller.ResponseMessage;
import com.zhangzhankui.samples.common.core.enums.ResponseEnum;
import com.zhangzhankui.samples.common.core.exception.BusinessException;
import com.zhangzhankui.samples.common.core.exception.DataNotFoundException;
import com.zhangzhankui.samples.common.core.exception.ForbiddenException;
import com.zhangzhankui.samples.common.core.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseMessage<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 - URL: {}, Message: {}", request.getRequestURI(), e.getMessage());
        return ResponseMessage.failed(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseMessage<Void> handleDataNotFoundException(DataNotFoundException e, HttpServletRequest request) {
        log.warn("数据不存在 - URL: {}, Message: {}", request.getRequestURI(), e.getMessage());
        return ResponseMessage.failed(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseMessage<Void> handleUnauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        log.warn("未授权 - URL: {}, Message: {}", request.getRequestURI(), e.getMessage());
        return ResponseMessage.failed(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseMessage<Void> handleForbiddenException(ForbiddenException e, HttpServletRequest request) {
        log.warn("禁止访问 - URL: {}, Message: {}", request.getRequestURI(), e.getMessage());
        return ResponseMessage.failed(e.getCode(), e.getMessage());
    }

    // ==================== Spring Security 异常 ====================

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseMessage<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("访问被拒绝 - URL: {}, Message: {}", request.getRequestURI(), e.getMessage());
        return ResponseMessage.failed(ResponseEnum.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseMessage<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证失败 - URL: {}, Message: {}", request.getRequestURI(), e.getMessage());
        return ResponseMessage.failed(ResponseEnum.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseMessage<Void> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        log.warn("凭证错误 - URL: {}, Message: {}", request.getRequestURI(), e.getMessage());
        return ResponseMessage.failed(ResponseEnum.PASSWORD_ERROR);
    }

    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseMessage<Void> handleDisabledException(DisabledException e, HttpServletRequest request) {
        log.warn("账号被禁用 - URL: {}, Message: {}", request.getRequestURI(), e.getMessage());
        return ResponseMessage.failed(ResponseEnum.ACCOUNT_DISABLED);
    }

    @ExceptionHandler(LockedException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseMessage<Void> handleLockedException(LockedException e, HttpServletRequest request) {
        log.warn("账号被锁定 - URL: {}, Message: {}", request.getRequestURI(), e.getMessage());
        return ResponseMessage.failed(ResponseEnum.ACCOUNT_LOCKED);
    }

    // ==================== 参数校验异常 ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<Void> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 - URL: {}, Message: {}", request.getRequestURI(), message);
        return ResponseMessage.failed(ResponseEnum.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败 - URL: {}, Message: {}", request.getRequestURI(), message);
        return ResponseMessage.failed(ResponseEnum.PARAM_BIND_ERROR, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<Void> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束校验失败 - URL: {}, Message: {}", request.getRequestURI(), message);
        return ResponseMessage.failed(ResponseEnum.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<Void> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少必填参数 - URL: {}, Parameter: {}", request.getRequestURI(), e.getParameterName());
        return ResponseMessage.failed(ResponseEnum.PARAM_MISSING, "缺少必填参数: " + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<Void> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型错误 - URL: {}, Parameter: {}", request.getRequestURI(), e.getName());
        return ResponseMessage.failed(ResponseEnum.PARAM_TYPE_ERROR, "参数类型错误: " + e.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<Void> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("请求体解析失败 - URL: {}, Message: {}", request.getRequestURI(), e.getMessage());
        return ResponseMessage.failed(ResponseEnum.BAD_REQUEST, "请求体解析失败");
    }

    // ==================== HTTP异常 ====================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseMessage<Void> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持 - URL: {}, Method: {}", request.getRequestURI(), e.getMethod());
        return ResponseMessage.failed(ResponseEnum.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResponseMessage<Void> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        log.warn("不支持的媒体类型 - URL: {}, MediaType: {}", request.getRequestURI(), e.getContentType());
        return ResponseMessage.failed(ResponseEnum.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseMessage<Void> handleNoHandlerFoundException(Exception e, HttpServletRequest request) {
        log.warn("资源不存在 - URL: {}", request.getRequestURI());
        return ResponseMessage.failed(ResponseEnum.NOT_FOUND);
    }

    // ==================== 其他异常 ====================

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<Void> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数 - URL: {}, Message: {}", request.getRequestURI(), e.getMessage());
        return ResponseMessage.failed(ResponseEnum.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseMessage<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 - URL: {}, Message: {}", request.getRequestURI(), e.getMessage(), e);
        return ResponseMessage.failed(ResponseEnum.INTERNAL_SERVER_ERROR, "系统繁忙，请稍后重试");
    }
}
