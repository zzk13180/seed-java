package com.zhangzhankui.seed.common.core.exception;

import java.io.Serial;

/** 用户异常 */
public class UserException extends ServiceException {

  @Serial private static final long serialVersionUID = 1L;

  public UserException(String message) {
    super(401, message);
  }

  public UserException(int code, String message) {
    super(code, message);
  }
}
