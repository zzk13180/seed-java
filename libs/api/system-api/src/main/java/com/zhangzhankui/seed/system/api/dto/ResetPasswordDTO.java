package com.zhangzhankui.seed.system.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 重置密码请求 DTO
 */
@Schema(description = "重置密码请求")
public record ResetPasswordDTO(
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "用户ID不能为空")
        Long userId,
    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
        String password) {}
