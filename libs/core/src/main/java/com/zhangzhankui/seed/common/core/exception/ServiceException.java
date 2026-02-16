package com.zhangzhankui.seed.common.core.exception;

import java.io.Serial;

import lombok.Getter;

/** 业务异常基类 */
@Getter
public class ServiceException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  /** 错误码 */
  private final int code;

  /** 错误详情 */
  private final String detailMessage;

  public ServiceException(String message) {
    this(400, message, null);
  }

  public ServiceException(int code, String message) {
    this(code, message, null);
  }

  public ServiceException(int code, String message, String detailMessage) {
    super(message);
    this.code = code;
    this.detailMessage = detailMessage;
  }

  @Override
  public String getMessage() {
    return super.getMessage();
  }
}
