package com.zhangzhankui.seed.system.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 修改状态请求 DTO
 */
@Schema(description = "修改状态请求")
public record ChangeStatusDTO(
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "用户ID不能为空")
        Long userId,
    @Schema(description = "状态 0-禁用 1-正常", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "状态不能为空")
        @Min(value = 0, message = "状态值无效")
        @Max(value = 1, message = "状态值无效")
        Integer status) {}
