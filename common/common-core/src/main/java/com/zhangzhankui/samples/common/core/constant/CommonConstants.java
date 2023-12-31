package com.zhangzhankui.samples.common.core.constant;

/**
 * 系统常量
 */
public final class CommonConstants {

    private CommonConstants() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== 日期格式 ====================

    /**
     * 日期格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 时间格式
     */
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * 日期时间格式
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // ==================== 请求头相关 ====================

    /**
     * 追踪ID请求头
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
}
