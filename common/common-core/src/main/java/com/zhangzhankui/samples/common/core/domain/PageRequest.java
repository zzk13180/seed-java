package com.zhangzhankui.samples.common.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询请求基类
 */
@Data
@Schema(description = "分页查询请求")
public class PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页码", example = "1", minimum = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10", minimum = "1", maximum = "100")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer pageSize = 10;

    @Schema(description = "排序字段")
    private String orderBy;

    @Schema(description = "排序方向 ASC/DESC")
    private String orderDirection = "DESC";

    /**
     * 获取分页偏移量
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 是否升序
     */
    public boolean isAsc() {
        return "ASC".equalsIgnoreCase(orderDirection);
    }
}
