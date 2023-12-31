package com.zhangzhankui.samples.common.core.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhangzhankui.samples.common.core.enums.ResponseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一API响应结果封装
 *
 * @param <T> 响应数据类型
 */
@Data
@Accessors(chain = true)
@Schema(description = "统一响应结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseMessage<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "状态码", example = "0")
    private int code;

    @Schema(description = "响应消息", example = "Success")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "时间戳")
    private LocalDateTime timestamp;

    @Schema(description = "请求追踪ID")
    private String traceId;

    public ResponseMessage() {
        this.timestamp = LocalDateTime.now();
    }

    // ==================== 成功响应 ====================

    public static <T> ResponseMessage<T> ok() {
        return result(null, ResponseEnum.SUCCESS);
    }

    public static <T> ResponseMessage<T> ok(T data) {
        return result(data, ResponseEnum.SUCCESS);
    }

    public static <T> ResponseMessage<T> ok(T data, String message) {
        return result(data, ResponseEnum.SUCCESS.getCode(), message);
    }

    // ==================== 失败响应 ====================

    public static <T> ResponseMessage<T> failed() {
        return result(null, ResponseEnum.ERROR);
    }

    public static <T> ResponseMessage<T> failed(String message) {
        return result(null, ResponseEnum.CUSTOM_ERROR.getCode(), message);
    }

    public static <T> ResponseMessage<T> failed(ResponseEnum responseEnum) {
        return result(null, responseEnum);
    }

    public static <T> ResponseMessage<T> failed(int code, String message) {
        return result(null, code, message);
    }

    public static <T> ResponseMessage<T> failed(ResponseEnum responseEnum, String message) {
        return result(null, responseEnum.getCode(), message);
    }

    // ==================== 工具方法 ====================

    private static <T> ResponseMessage<T> result(T data, ResponseEnum responseEnum) {
        return result(data, responseEnum.getCode(), responseEnum.getMessage());
    }

    private static <T> ResponseMessage<T> result(T data, int code, String message) {
        ResponseMessage<T> result = new ResponseMessage<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 设置追踪ID
     */
    public ResponseMessage<T> traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == ResponseEnum.SUCCESS.getCode();
    }
}
