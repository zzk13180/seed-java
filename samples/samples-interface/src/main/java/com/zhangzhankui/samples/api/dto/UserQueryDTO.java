package com.zhangzhankui.samples.api.dto;

import com.zhangzhankui.samples.common.core.domain.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 用户查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询请求")
public class UserQueryDTO extends PageRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关键词搜索 (用户名/昵称/邮箱)")
    private String keyword;

    @Schema(description = "状态: 0-禁用 1-启用")
    private Integer status;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;
}
