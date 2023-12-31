package com.zhangzhankui.samples.common.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 带审计字段的基础实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 是否删除 (软删除标记)
     */
    private Boolean deleted = false;
}
