package com.zhangzhankui.samples.common.core.exception;

import com.zhangzhankui.samples.common.core.enums.ResponseEnum;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常基类
 * <p>
 * 所有业务异常都应继承此类
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误消息
     */
    private final String message;

    public BusinessException(String message) {
        super(message);
        this.code = ResponseEnum.BUSINESS_ERROR.getCode();
        this.message = message;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResponseEnum responseEnum) {
        super(responseEnum.getMessage());
        this.code = responseEnum.getCode();
        this.message = responseEnum.getMessage();
    }

    public BusinessException(ResponseEnum responseEnum, String message) {
        super(message);
        this.code = responseEnum.getCode();
        this.message = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResponseEnum.BUSINESS_ERROR.getCode();
        this.message = message;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 快速创建业务异常
     */
    public static BusinessException of(String message) {
        return new BusinessException(message);
    }

    public static BusinessException of(ResponseEnum responseEnum) {
        return new BusinessException(responseEnum);
    }

    public static BusinessException of(ResponseEnum responseEnum, String message) {
        return new BusinessException(responseEnum, message);
    }

    public static BusinessException of(int code, String message) {
        return new BusinessException(code, message);
    }
}
