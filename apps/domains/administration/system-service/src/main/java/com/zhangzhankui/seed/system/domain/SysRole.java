package com.zhangzhankui.seed.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhangzhankui.seed.common.datasource.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 系统角色实体 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

  /** 角色ID */
  @TableId private Long roleId;

  /** 角色名称 */
  private String roleName;

  /** 角色权限字符 */
  private String roleKey;

  /** 显示顺序 */
  private Integer sort;

  /** 数据范围 1-全部 2-自定义 3-本部门 4-本部门及以下 5-仅本人 */
  private Integer dataScope;

  /** 状态 0-禁用 1-正常 */
  private Integer status;
}
