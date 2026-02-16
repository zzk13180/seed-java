package com.zhangzhankui.seed.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zhangzhankui.seed.common.datasource.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 系统用户实体 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

  /** 用户ID */
  @TableId private Long userId;

  /** 用户名 */
  private String username;

  /** 密码（JSON序列化时忽略，防止泄露） */
  @JsonIgnore private String password;

  /** 昵称 */
  private String nickname;

  /** 邮箱 */
  private String email;

  /** 手机号 */
  private String phone;

  /** 性别 0-女 1-男 2-未知 */
  private Integer sex;

  /** 头像 */
  private String avatar;

  /** 部门ID */
  private Long deptId;

  /** 状态 0-禁用 1-正常 */
  private Integer status;
}
