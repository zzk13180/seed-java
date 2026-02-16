package com.zhangzhankui.seed.common.core.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

import lombok.Data;

/** 分页查询参数 */
@Data
public class PageQuery implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 允许排序的列名白名单 */
  private static final Set<String> ALLOWED_ORDER_COLUMNS = Set.of(
      "create_time", "update_time", "user_id", "username", "status", "dept_id"
  );

  /** 当前页码 */
  private Integer pageNum = 1;

  /** 每页数量 */
  private Integer pageSize = 10;

  /** 排序字段 */
  private String orderByColumn;

  /** 排序方向 asc/desc */
  private String orderDirection = "asc";

  /** 获取偏移量 */
  public long getOffset() {
    return (long) (pageNum - 1) * pageSize;
  }

  /**
   * 获取经过白名单校验的排序字段
   * @return 合法的排序字段名，不合法则返回 null
   */
  public String getSafeOrderByColumn() {
    if (orderByColumn == null || orderByColumn.isBlank()) {
      return null;
    }
    String col = orderByColumn.trim().toLowerCase();
    return ALLOWED_ORDER_COLUMNS.contains(col) ? col : null;
  }

  /**
   * 获取经过校验的排序方向
   * @return "asc" 或 "desc"
   */
  public String getSafeOrderDirection() {
    return "desc".equalsIgnoreCase(orderDirection) ? "desc" : "asc";
  }

}
