package com.zhangzhankui.seed.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhangzhankui.seed.common.datasource.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 系统菜单实体 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

  /** 菜单ID */
  @TableId private Long menuId;

  /** 菜单名称 */
  private String menuName;

  /** 父菜单ID */
  private Long parentId;

  /** 显示顺序 */
  private Integer sort;

  /** 路由地址 */
  private String path;

  /** 组件路径 */
  private String component;

  /** 路由参数 */
  private String query;

  /** 菜单类型 M-目录 C-菜单 F-按钮 */
  private String menuType;

  /** 是否显示 0-隐藏 1-显示 */
  private Integer visible;

  /** 状态 0-禁用 1-正常 */
  private Integer status;

  /** 权限标识 */
  private String perms;

  /** 菜单图标 */
  private String icon;

  /** 是否缓存 0-不缓存 1-缓存 */
  private Integer isCache;

  /** 是否外链 0-否 1-是 */
  private Integer isFrame;
}
