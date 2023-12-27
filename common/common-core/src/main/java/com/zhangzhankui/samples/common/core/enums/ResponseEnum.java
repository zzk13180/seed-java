package com.zhangzhankui.samples.common.core.enums;

public enum ResponseEnum {
  SUCCESS(0, "成功"),
  ERROR(-1, "错误"),
  BAD_ARGUMENT(400, "参数错误"),
  UNAUTHORIZED(401, "未授权"),
  FORBIDDEN(403, "禁止访问"),
  NOT_FOUND(404, "资源不存在"),
  INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
  SERVICE_UNAVAILABLE(503, "服务不可用");

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
