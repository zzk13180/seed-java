package com.zhangzhankui.samples.common.core.enums;

public enum ResponseEnum {
  SUCCESS(0, "Success"), // 成功
  ERROR(-1, "Error"), // 错误

  BAD_ARGUMENT(400, "Bad Argument"), // 参数错误
  UNAUTHORIZED(401, "Unauthorized"), // 未授权
  FORBIDDEN(403, "Forbidden"), // 禁止访问
  NOT_FOUND(404, "Resource Not Found"), // 资源不存在
  INTERNAL_SERVER_ERROR(500, "Internal Server Error"), // 服务器内部错误
  SERVICE_UNAVAILABLE(503, "Service Unavailable"), // 服务不可用
  BAD_GATEWAY(502, "Bad Gateway"), // 错误网关
  REQUEST_TIMEOUT(408, "Request Timeout"), // 请求超时
  CONFLICT(409, "Conflict"), // 冲突
  GONE(410, "Gone"), // 已删除
  LENGTH_REQUIRED(411, "Length Required"), // 需要长度
  PRECONDITION_FAILED(412, "Precondition Failed"), // 前提条件失败
  PAYLOAD_TOO_LARGE(413, "Payload Too Large"), // 载荷过大
  URI_TOO_LONG(414, "URI Too Long"), // URI过长
  UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"), // 不支持的媒体类型
  TOO_MANY_REQUESTS(429, "Too Many Requests"), // 请求过多
  UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"), // 因法律原因不可用

  CUSTOM_ERROR(1000, "Custom Error"); // 自定义错误

  private final int errorCode;
  private final String errorMessage;

  ResponseEnum(int errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

}
