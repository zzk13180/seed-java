package com.zhangzhankui.seed.system.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户数据传输对象 - 用于创建和更新
 *
 * <p>使用 Java Record 类型，不可变且自动生成 equals/hashCode/toString
 * <p>注意：新增用户时 password 必填，更新用户时 password 可选（由 Controller 层控制）
 */
@Schema(description = "用户数据传输对象")
public record SysUserDTO(
    @Schema(description = "用户ID（更新时必填）") Long userId,
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "用户名不能为空")
        @Size(min = 2, max = 30, message = "用户名长度必须在2-30之间")
        String username,
    @Schema(description = "密码（新增时必填）")
        @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
        String password,
    @Schema(description = "昵称") @Size(max = 30, message = "昵称长度不能超过30") String nickname,
    @Schema(description = "邮箱") @Email(message = "邮箱格式不正确") @Size(max = 50, message = "邮箱长度不能超过50")
        String email,
    @Schema(description = "手机号") @Size(max = 11, message = "手机号长度不能超过11") String phone,
    @Schema(description = "性别 0-女 1-男 2-未知") Integer sex,
    @Schema(description = "头像") String avatar,
    @Schema(description = "部门ID") Long deptId,
    @Schema(description = "状态 0-禁用 1-正常") Integer status,
    @Schema(description = "备注") @Size(max = 500, message = "备注长度不能超过500") String remark) {}
