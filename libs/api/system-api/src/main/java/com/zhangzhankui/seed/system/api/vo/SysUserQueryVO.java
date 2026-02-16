package com.zhangzhankui.seed.system.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户查询条件对象
 *
 * <p>使用 Java Record 类型，不可变且自动生成 equals/hashCode/toString
 */
@Schema(description = "用户查询条件")
public record SysUserQueryVO(
    @Schema(description = "用户名（模糊查询）") String username,
    @Schema(description = "昵称（模糊查询）") String nickname,
    @Schema(description = "手机号") String phone,
    @Schema(description = "部门ID") Long deptId,
    @Schema(description = "状态 0-禁用 1-正常") Integer status) {}
