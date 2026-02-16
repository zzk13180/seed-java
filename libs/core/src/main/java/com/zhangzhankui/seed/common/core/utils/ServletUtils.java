package com.zhangzhankui.seed.common.core.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Servlet 工具类
 *
 * <p>纯 Java 原生实现，无第三方依赖
 */
public class ServletUtils {

  /** 获取请求 */
  public static HttpServletRequest getRequest() {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
      return servletRequestAttributes.getRequest();
    }
    return null;
  }

  /** 获取请求参数 */
  public static String getParameter(String name) {
    HttpServletRequest request = getRequest();
    return request != null ? request.getParameter(name) : null;
  }

  /** 获取请求参数（Integer） */
  public static Integer getParameterToInt(String name) {
    String value = getParameter(name);
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /** 获取请求参数（Long） */
  public static Long getParameterToLong(String name) {
    String value = getParameter(name);
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /** 获取请求头 */
  public static String getHeader(String name) {
    HttpServletRequest request = getRequest();
    return request != null ? request.getHeader(name) : null;
  }

  /** 获取客户端IP */
  public static String getClientIp() {
    HttpServletRequest request = getRequest();
    if (request == null) {
      return "unknown";
    }

    String ip = request.getHeader("X-Forwarded-For");
    if (isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }

    // 多个代理时取第一个
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }

    return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
  }

  /** 判断是否是 Ajax 请求 */
  public static boolean isAjaxRequest() {
    HttpServletRequest request = getRequest();
    if (request == null) {
      return false;
    }
    String accept = request.getHeader("Accept");
    String xRequestedWith = request.getHeader("X-Requested-With");
    return (accept != null && accept.contains("application/json"))
        || "XMLHttpRequest".equals(xRequestedWith);
  }

  /** 判断字符串是否为空或空白 */
  private static boolean isBlank(String str) {
    return str == null || str.isBlank();
  }
}
