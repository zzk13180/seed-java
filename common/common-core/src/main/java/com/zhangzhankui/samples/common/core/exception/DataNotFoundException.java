package com.zhangzhankui.samples.common.core.exception;

import com.zhangzhankui.samples.common.core.enums.ResponseEnum;

import java.io.Serial;

/**
 * 数据不存在异常
 */
public class DataNotFoundException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DataNotFoundException() {
        super(ResponseEnum.DATA_NOT_EXIST);
    }

    public DataNotFoundException(String message) {
        super(ResponseEnum.DATA_NOT_EXIST, message);
    }

    public DataNotFoundException(String entityName, Object id) {
        super(ResponseEnum.DATA_NOT_EXIST, String.format("%s [%s] 不存在", entityName, id));
    }
}
