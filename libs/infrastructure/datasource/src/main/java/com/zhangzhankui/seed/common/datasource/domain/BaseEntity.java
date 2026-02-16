package com.zhangzhankui.seed.common.datasource.domain;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据层实体基类
 *
 * <p>继承核心层 BaseEntity（抽象接口），实现所有 getter/setter 并添加 MyBatis-Plus 注解，
 * 使核心层保持对持久层框架的零依赖。
 *
 * <p>扩展功能：
 * 1. 审计字段自动填充 (@TableField fill)
 * 2. 逻辑删除 (@TableLogic)
 * 3. 数据权限过滤SQL
 * 4. 扩展参数
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseEntity extends com.zhangzhankui.seed.common.core.base.BaseEntity {

  // ==================== 审计字段 ====================

  /** 创建者（用户名） */
  @TableField(fill = FieldFill.INSERT)
  private String createBy;

  /** 创建时间 */
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  /** 更新者（用户名） */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private String updateBy;

  /** 更新时间 */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;

  /** 备注 */
  private String remark;

  /** 删除标志（0存在 1删除） */
  @TableLogic
  private Integer deleted;

  /** 租户ID */
  @TableField(fill = FieldFill.INSERT)
  private String tenantId;

  // ==================== 数据层扩展字段 ====================

  /** 数据权限过滤SQL (不映射到数据库) */
  @TableField(exist = false)
  @JsonIgnore
  private String dataScopeSql;

  /** 扩展参数 (不映射到数据库) */
  @TableField(exist = false)
  @JsonIgnore
  private Map<String, Object> params;

  /** 获取扩展参数 */
  public Map<String, Object> getParams() {
    if (params == null) {
      params = new HashMap<>();
    }
    return params;
  }

  /** 添加扩展参数 */
  public void addParam(String key, Object value) {
    getParams().put(key, value);
  }
}
