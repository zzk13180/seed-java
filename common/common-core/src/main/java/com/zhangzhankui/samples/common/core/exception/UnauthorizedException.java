package com.zhangzhankui.samples.common.core.exception;

import com.zhangzhankui.samples.common.core.enums.ResponseEnum;

import java.io.Serial;

/**
 * 未授权异常
 */
public class UnauthorizedException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UnauthorizedException() {
        super(ResponseEnum.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(ResponseEnum.UNAUTHORIZED, message);
    }
}
