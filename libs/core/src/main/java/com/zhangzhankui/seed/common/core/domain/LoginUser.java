package com.zhangzhankui.seed.common.core.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/** 登录用户信息 */
@Data
public class LoginUser implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 用户ID */
  private Long userId;

  /** 用户名 */
  private String username;

  /** 密码（序列化时忽略，防止泄露） */
  @JsonIgnore
  private String password;

  /** 昵称 */
  private String nickname;

  /** 邮箱 */
  private String email;

  /** 手机号 */
  private String phone;

  /** 头像 */
  private String avatar;

  /** 状态 0-禁用 1-正常 */
  private Integer status;

  /** 部门ID */
  private Long deptId;

  /** 租户ID */
  private String tenantId;

  /** 用户类型 */
  private String userType;

  /** Token */
  private String token;

  /** 登录IP */
  private String loginIp;

  /** 登录时间 */
  private Long loginTime;

  /** 过期时间 */
  private Long expireTime;

  /** 角色列表 */
  private Set<String> roles;

  /** 权限列表 */
  private Set<String> permissions;
}
