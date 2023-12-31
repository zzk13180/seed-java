package com.zhangzhankui.samples.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应状态码枚举
 * <p>
 * 状态码规范:
 * - 0: 成功
 * - 1xxx: 客户端错误
 * - 2xxx: 服务端错误
 * - 3xxx: 业务错误
 * - 4xx/5xx: 兼容HTTP状态码
 */
@Getter
@AllArgsConstructor
public enum ResponseEnum {

    // ==================== 成功 ====================
    SUCCESS(0, "操作成功"),

    // ==================== 客户端错误 1xxx ====================
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或认证失败"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    REQUEST_TIMEOUT(408, "请求超时"),
    CONFLICT(409, "资源冲突"),
    PAYLOAD_TOO_LARGE(413, "请求体过大"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的媒体类型"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // ==================== 服务端错误 5xx/2xxx ====================
    ERROR(-1, "操作失败"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    BAD_GATEWAY(502, "网关错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),

    // ==================== 认证授权相关 1001-1099 ====================
    TOKEN_EXPIRED(1001, "登录已过期，请重新登录"),
    TOKEN_INVALID(1002, "无效的令牌"),
    ACCOUNT_DISABLED(1003, "账号已被禁用"),
    ACCOUNT_LOCKED(1004, "账号已被锁定"),
    PASSWORD_ERROR(1005, "密码错误"),
    ACCOUNT_NOT_FOUND(1006, "账号不存在"),
    CAPTCHA_ERROR(1007, "验证码错误"),
    CAPTCHA_EXPIRED(1008, "验证码已过期"),

    // ==================== 参数校验相关 1100-1199 ====================
    VALIDATION_ERROR(1100, "参数校验失败"),
    PARAM_MISSING(1101, "必填参数缺失"),
    PARAM_TYPE_ERROR(1102, "参数类型错误"),
    PARAM_BIND_ERROR(1103, "参数绑定失败"),

    // ==================== 业务错误 3xxx ====================
    BUSINESS_ERROR(3000, "业务处理异常"),
    DATA_NOT_EXIST(3001, "数据不存在"),
    DATA_ALREADY_EXIST(3002, "数据已存在"),
    DATA_SAVE_ERROR(3003, "数据保存失败"),
    DATA_UPDATE_ERROR(3004, "数据更新失败"),
    DATA_DELETE_ERROR(3005, "数据删除失败"),

    // ==================== 自定义错误 ====================
    CUSTOM_ERROR(9999, "自定义错误");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态信息
     */
    private final String message;
}
