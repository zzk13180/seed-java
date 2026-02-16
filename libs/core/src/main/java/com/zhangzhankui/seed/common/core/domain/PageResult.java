package com.zhangzhankui.seed.common.core.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 分页结果
 *
 * <p>领域层分页结果对象，不依赖任何 ORM 框架
 *
 * @param <T> 数据类型
 */
@Data
@Accessors(chain = true)
public class PageResult<T> implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 当前页码 */
  private long pageNum;

  /** 每页数量 */
  private long pageSize;

  /** 总记录数 */
  private long total;

  /** 总页数 */
  private long pages;

  /** 数据列表 */
  private List<T> records;

  public PageResult() {
    this.records = Collections.emptyList();
  }

  public PageResult(long pageNum, long pageSize, long total, List<T> records) {
    this.pageNum = pageNum;
    this.pageSize = pageSize;
    this.total = total;
    this.records = records != null ? records : Collections.emptyList();
    this.pages = pageSize > 0 ? (total + pageSize - 1) / pageSize : 0;
  }

  public static <T> PageResult<T> empty() {
    return new PageResult<>(1, 10, 0, Collections.emptyList());
  }

  public static <T> PageResult<T> of(long pageNum, long pageSize, long total, List<T> records) {
    return new PageResult<>(pageNum, pageSize, total, records);
  }

  /**
   * 转换分页结果中的数据类型
   *
   * @param mapper 转换函数
   * @param <R> 目标类型
   * @return 转换后的分页结果
   */
  public <R> PageResult<R> map(Function<T, R> mapper) {
    List<R> mappedRecords = this.records.stream()
        .map(mapper)
        .collect(Collectors.toList());
    return new PageResult<>(this.pageNum, this.pageSize, this.total, mappedRecords);
  }

  /**
   * 判断是否有下一页
   */
  public boolean hasNext() {
    return pageNum < pages;
  }

  /**
   * 判断是否有上一页
   */
  public boolean hasPrevious() {
    return pageNum > 1;
  }

  /**
   * 判断是否为空
   */
  public boolean isEmpty() {
    return records == null || records.isEmpty();
  }
}
