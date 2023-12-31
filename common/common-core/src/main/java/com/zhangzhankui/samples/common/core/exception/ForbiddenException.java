package com.zhangzhankui.samples.common.core.exception;

import com.zhangzhankui.samples.common.core.enums.ResponseEnum;

import java.io.Serial;

/**
 * 禁止访问异常
 */
public class ForbiddenException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ForbiddenException() {
        super(ResponseEnum.FORBIDDEN);
    }

    public ForbiddenException(String message) {
        super(ResponseEnum.FORBIDDEN, message);
    }
}
