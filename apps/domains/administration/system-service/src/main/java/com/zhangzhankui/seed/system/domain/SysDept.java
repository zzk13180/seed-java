package com.zhangzhankui.seed.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhangzhankui.seed.common.datasource.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 系统部门实体 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {

  /** 部门ID */
  @TableId private Long deptId;

  /** 父部门ID */
  private Long parentId;

  /** 祖级列表 */
  private String ancestors;

  /** 部门名称 */
  private String deptName;

  /** 显示顺序 */
  private Integer sort;

  /** 负责人 */
  private String leader;

  /** 联系电话 */
  private String phone;

  /** 邮箱 */
  private String email;

  /** 状态 0-禁用 1-正常 */
  private Integer status;
}
