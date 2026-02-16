package com.zhangzhankui.seed.common.web.controller;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.zhangzhankui.seed.common.core.domain.ApiResult;
import com.zhangzhankui.seed.common.security.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 基础控制器
 *
 * <p>提供： 1. 通用参数绑定 2. 便捷方法 3. 响应封装
 */
public abstract class BaseController {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /**
   * 参数绑定配置
   *
   * <p>自动转换日期类型参数
   */
  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    // LocalDate 转换
    binder.registerCustomEditor(
        LocalDate.class,
        new PropertyEditorSupport() {
          @Override
          public void setAsText(String text) {
            if (StringUtils.isNotBlank(text)) {
              setValue(LocalDate.parse(text, DATE_FORMATTER));
            } else {
              setValue(null);
            }
          }
        });

    // LocalDateTime 转换
    binder.registerCustomEditor(
        LocalDateTime.class,
        new PropertyEditorSupport() {
          @Override
          public void setAsText(String text) {
            if (StringUtils.isNotBlank(text)) {
              if (text.length() == 10) {
                setValue(LocalDate.parse(text, DATE_FORMATTER).atStartOfDay());
              } else {
                setValue(LocalDateTime.parse(text, DATETIME_FORMATTER));
              }
            } else {
              setValue(null);
            }
          }
        });
  }

  // ==================== 便捷方法 ====================

  /** 获取当前请求 */
  protected HttpServletRequest getRequest() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return attrs != null ? attrs.getRequest() : null;
  }

  /** 获取当前响应 */
  protected HttpServletResponse getResponse() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return attrs != null ? attrs.getResponse() : null;
  }

  /** 获取请求参数 */
  protected String getParameter(String name) {
    HttpServletRequest request = getRequest();
    if (request != null) {
      return request.getParameter(name);
    }
    return null;
  }

  /** 获取请求参数（带默认值） */
  protected String getParameter(String name, String defaultValue) {
    String value = getParameter(name);
    return StringUtils.isBlank(value) ? defaultValue : value;
  }

  /** 获取整数参数 */
  protected Integer getParameterInt(String name) {
    String value = getParameter(name);
    if (StringUtils.isNotBlank(value)) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }

  /** 获取整数参数（带默认值） */
  protected Integer getParameterInt(String name, Integer defaultValue) {
    Integer value = getParameterInt(name);
    return value != null ? value : defaultValue;
  }

  /** 获取当前用户ID */
  protected Long getCurrentUserId() {
    try {
      return SecurityUtils.getUserId();
    } catch (Exception e) {
      return null;
    }
  }

  /** 获取当前用户名 */
  protected String getCurrentUsername() {
    try {
      return SecurityUtils.getUsername();
    } catch (Exception e) {
      return null;
    }
  }

  // ==================== 响应封装 ====================

  /** 成功响应 */
  protected <T> ApiResult<T> success() {
    return ApiResult.ok();
  }

  /** 成功响应（带数据） */
  protected <T> ApiResult<T> success(T data) {
    return ApiResult.ok(data);
  }

  /** 成功响应（带消息） */
  protected <T> ApiResult<T> success(String msg) {
    return ApiResult.ok(null, msg);
  }

  /** 成功响应（带数据和消息） */
  protected <T> ApiResult<T> success(T data, String msg) {
    return ApiResult.ok(data, msg);
  }

  /** 失败响应 */
  protected <T> ApiResult<T> fail() {
    return ApiResult.fail();
  }

  /** 失败响应（带消息） */
  protected <T> ApiResult<T> fail(String msg) {
    return ApiResult.fail(msg);
  }

  /** 根据条件返回响应 */
  protected ApiResult<Void> toAjax(boolean result) {
    return result ? ApiResult.ok() : ApiResult.fail("操作失败");
  }

  /** 根据影响行数返回响应 */
  protected ApiResult<Void> toAjax(int rows) {
    return rows > 0 ? ApiResult.ok() : ApiResult.fail("操作失败");
  }
}
