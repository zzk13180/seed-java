package com.zhangzhankui.samples.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户信息VO
 */
@Data
@Schema(description = "用户信息")
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "状态: 0-禁用 1-启用")
    private Integer status;

    @Schema(description = "角色列表")
    private Set<String> roles;

    @Schema(description = "权限列表")
    private Set<String> permissions;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
