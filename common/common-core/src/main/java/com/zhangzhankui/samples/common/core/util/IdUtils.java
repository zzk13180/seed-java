package com.zhangzhankui.samples.common.core.util;

import java.util.UUID;

/**
 * ID生成工具类
 */
public final class IdUtils {

    private IdUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 生成UUID (无连字符)
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成UUID (带连字符)
     */
    public static String uuidWithHyphen() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成追踪ID
     */
    public static String traceId() {
        return uuid().substring(0, 16);
    }
}
