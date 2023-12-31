package com.zhangzhankui.samples.common.core.exception;

import com.zhangzhankui.samples.common.core.enums.ResponseEnum;

import java.io.Serial;

/**
 * 数据已存在异常
 */
public class DataAlreadyExistsException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DataAlreadyExistsException() {
        super(ResponseEnum.DATA_ALREADY_EXIST);
    }

    public DataAlreadyExistsException(String message) {
        super(ResponseEnum.DATA_ALREADY_EXIST, message);
    }

    public DataAlreadyExistsException(String entityName, String field, Object value) {
        super(ResponseEnum.DATA_ALREADY_EXIST, 
              String.format("%s [%s=%s] 已存在", entityName, field, value));
    }
}
