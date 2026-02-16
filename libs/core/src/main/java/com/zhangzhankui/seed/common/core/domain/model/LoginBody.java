package com.zhangzhankui.seed.common.core.domain.model;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求体
 *
 * <p>安全说明：登录保护由服务端实现 - IP 级别限流 - 登录失败次数限制和账户锁定 - BCrypt 密码验证
 */
@Data
public class LoginBody implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 用户名 */
  @NotBlank(message = "用户名不能为空")
  private String username;

  /** 密码 */
  @NotBlank(message = "密码不能为空")
  private String password;

  /** 租户ID */
  private String tenantId;

  /** 设备指纹（可选，用于增强安全） */
  private String deviceFingerprint;
}
