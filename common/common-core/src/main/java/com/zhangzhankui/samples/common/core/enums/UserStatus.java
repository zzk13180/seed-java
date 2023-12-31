package com.zhangzhankui.samples.common.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
@AllArgsConstructor
public enum UserStatus {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 启用
     */
    ENABLED(1, "启用");

    /**
     * 状态值
     */
    private final Integer value;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * JSON 序列化时使用 value
     */
    @JsonValue
    public Integer getValue() {
        return value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static UserStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown UserStatus value: " + value);
    }

    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return this == ENABLED;
    }

    /**
     * 判断是否禁用
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }
}
