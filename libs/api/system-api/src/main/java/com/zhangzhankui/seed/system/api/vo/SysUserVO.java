package com.zhangzhankui.seed.system.api.vo;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户视图对象 - 用于返回给前端，不包含敏感信息
 *
 * <p>使用 Java Record 类型，不可变且自动生成 equals/hashCode/toString
 */
@Schema(description = "用户视图对象")
public record SysUserVO(
    @Schema(description = "用户ID") Long userId,
    @Schema(description = "用户名") String username,
    @Schema(description = "昵称") String nickname,
    @Schema(description = "邮箱") String email,
    @Schema(description = "手机号（脱敏）") String phone,
    @Schema(description = "性别 0-女 1-男 2-未知") Integer sex,
    @Schema(description = "头像") String avatar,
    @Schema(description = "部门ID") Long deptId,
    @Schema(description = "部门名称") String deptName,
    @Schema(description = "状态 0-禁用 1-正常") Integer status,
    @Schema(description = "备注") String remark,
    @Schema(description = "创建时间") LocalDateTime createTime,
    @Schema(description = "更新时间") LocalDateTime updateTime) {}
