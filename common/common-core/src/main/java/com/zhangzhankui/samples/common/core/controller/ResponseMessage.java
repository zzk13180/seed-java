package com.zhangzhankui.samples.common.core.controller;

import java.io.Serializable;
import java.util.Optional;
import com.zhangzhankui.samples.common.core.enums.ResponseEnum;

public class ResponseMessage<T> implements Serializable {
  private static final long serialVersionUID = 7526472295622776147L;

  private int errorCode;
  private String errorMessage;
  private Optional<T> data;

  public static <T> ResponseMessage<T> ok() {
    return result(null, ResponseEnum.SUCCESS.getErrorCode(), ResponseEnum.SUCCESS.getErrorMessage());
  }

  public static <T> ResponseMessage<T> ok(T data) {
    return result(data, ResponseEnum.SUCCESS.getErrorCode(), ResponseEnum.SUCCESS.getErrorMessage());
  }

  public static <T> ResponseMessage<T> failed() {
    return result(null, ResponseEnum.ERROR.getErrorCode(), ResponseEnum.ERROR.getErrorMessage());
  }

  public static <T> ResponseMessage<T> failed(String errorMessage) {
    return result(null, ResponseEnum.CUSTOM_ERROR.getErrorCode(), errorMessage);
  }

  public static <T> ResponseMessage<T> failed(ResponseEnum responseEnum) {
    return result(null, responseEnum.getErrorCode(), responseEnum.getErrorMessage());
  }

  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setData(T data) {
    this.data = Optional.ofNullable(data);
  }

  public Optional<T> getData() {
    return data;
  }

  private static <T> ResponseMessage<T> result(T data, int code, String msg) {
    ResponseMessage<T> result = new ResponseMessage<>();
    result.setErrorCode(code);
    result.setData(data);
    result.setErrorMessage(msg);
    return result;
  }

}
