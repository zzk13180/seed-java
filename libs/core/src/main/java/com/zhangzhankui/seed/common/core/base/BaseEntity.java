package com.zhangzhankui.seed.common.core.base;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类（抽象接口定义）
 *
 * <p>所有业务实体都应继承此基类，提供通用的审计字段和软删除支持。
 * 此类不声明字段，仅定义 getter/setter 契约，
 * 字段由 datasource 模块的子类声明并添加 MyBatis-Plus 注解，
 * 避免字段遮蔽（field shadowing）导致的 equals/hashCode/序列化问题。
 */
public abstract class BaseEntity implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 创建者（用户名） */
  public abstract String getCreateBy();
  public abstract void setCreateBy(String createBy);

  /** 创建时间 */
  public abstract LocalDateTime getCreateTime();
  public abstract void setCreateTime(LocalDateTime createTime);

  /** 更新者（用户名） */
  public abstract String getUpdateBy();
  public abstract void setUpdateBy(String updateBy);

  /** 更新时间 */
  public abstract LocalDateTime getUpdateTime();
  public abstract void setUpdateTime(LocalDateTime updateTime);

  /** 备注 */
  public abstract String getRemark();
  public abstract void setRemark(String remark);

  /** 删除标志（0存在 1删除） */
  public abstract Integer getDeleted();
  public abstract void setDeleted(Integer deleted);

  /** 租户ID */
  public abstract String getTenantId();
  public abstract void setTenantId(String tenantId);
}
