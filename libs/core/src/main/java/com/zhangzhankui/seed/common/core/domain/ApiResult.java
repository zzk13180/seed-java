package com.zhangzhankui.seed.common.core.domain;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 统一API响应结果
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ApiResult<T> implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  // ========== 状态码常量 ==========

  /** 成功 */
  public static final int SUCCESS = 200;

  /** 参数错误 */
  public static final int BAD_REQUEST = 400;

  /** 未认证 */
  public static final int UNAUTHORIZED = 401;

  /** 无权限 */
  public static final int FORBIDDEN = 403;

  /** 资源不存在 */
  public static final int NOT_FOUND = 404;

  /** 请求过于频繁 */
  public static final int TOO_MANY_REQUESTS = 429;

  /** 服务器错误 */
  public static final int FAIL = 500;

  /** 服务不可用 */
  public static final int SERVICE_UNAVAILABLE = 503;

  // ========== 字段 ==========

  /** 状态码 */
  private int code;

  /** 响应消息 */
  private String message;

  /** 响应数据 */
  private T data;

  // ========== 成功响应 ==========

  public static <T> ApiResult<T> ok() {
    return of(null, SUCCESS, "操作成功");
  }

  public static <T> ApiResult<T> ok(T data) {
    return of(data, SUCCESS, "操作成功");
  }

  public static <T> ApiResult<T> ok(T data, String message) {
    return of(data, SUCCESS, message);
  }

  // ========== 失败响应 ==========

  public static <T> ApiResult<T> fail() {
    return of(null, FAIL, "操作失败");
  }

  public static <T> ApiResult<T> fail(String message) {
    return of(null, FAIL, message);
  }

  public static <T> ApiResult<T> fail(int code, String message) {
    return of(null, code, message);
  }

  public static <T> ApiResult<T> fail(T data, String message) {
    return of(data, FAIL, message);
  }

  // ========== 常用快捷方法 ==========

  /** 参数错误 (400) */
  public static <T> ApiResult<T> badRequest(String message) {
    return of(null, BAD_REQUEST, message);
  }

  /** 未认证 (401) */
  public static <T> ApiResult<T> unauthorized() {
    return of(null, UNAUTHORIZED, "请先登录");
  }

  /** 未认证 (401) - 自定义消息 */
  public static <T> ApiResult<T> unauthorized(String message) {
    return of(null, UNAUTHORIZED, message);
  }

  /** 无权限 (403) */
  public static <T> ApiResult<T> forbidden() {
    return of(null, FORBIDDEN, "没有访问权限");
  }

  /** 无权限 (403) - 自定义消息 */
  public static <T> ApiResult<T> forbidden(String message) {
    return of(null, FORBIDDEN, message);
  }

  /** 资源不存在 (404) */
  public static <T> ApiResult<T> notFound(String resource) {
    return of(null, NOT_FOUND, resource + "不存在");
  }

  /** 请求过于频繁 (429) */
  public static <T> ApiResult<T> tooManyRequests() {
    return of(null, TOO_MANY_REQUESTS, "请求过于频繁，请稍后重试");
  }

  /** 服务不可用 (503) */
  public static <T> ApiResult<T> serviceUnavailable() {
    return of(null, SERVICE_UNAVAILABLE, "服务暂时不可用");
  }

  // ========== 状态判断 ==========

  /** 判断是否成功 */
  public boolean isSuccess() {
    return SUCCESS == this.code;
  }

  /** 判断是否失败 */
  public boolean isFail() {
    return !isSuccess();
  }

  // ========== 私有构造 ==========

  private static <T> ApiResult<T> of(T data, int code, String message) {
    ApiResult<T> result = new ApiResult<>();
    result.setCode(code);
    result.setData(data);
    result.setMessage(message);
    return result;
  }
}
