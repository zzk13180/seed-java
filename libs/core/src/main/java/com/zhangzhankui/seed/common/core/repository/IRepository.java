package com.zhangzhankui.seed.common.core.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.zhangzhankui.seed.common.core.domain.PageQuery;
import com.zhangzhankui.seed.common.core.domain.PageResult;

/**
 * 仓储接口 - 领域层数据访问抽象
 *
 * <p>遵循 DDD 仓储模式，定义领域对象的持久化操作。
 * 此接口位于 core 模块，不依赖任何具体 ORM 框架。
 *
 * <p>设计原则：
 * <ul>
 *   <li>框架无关 - 不引入任何 ORM 特定类型
 *   <li>领域驱动 - 方法命名体现领域语义
 *   <li>可测试性 - 易于 mock 和单元测试
 * </ul>
 *
 * @param <T> 实体类型
 * @param <K> 主键类型
 */
public interface IRepository<T, K extends Serializable> {

  // ==================== 持久化操作 ====================

  /**
   * 保存实体
   *
   * @param entity 实体对象
   * @return 保存后的实体（可能包含生成的ID）
   */
  T save(T entity);

  /**
   * 批量保存实体
   *
   * @param entities 实体集合
   * @return 保存后的实体列表
   */
  List<T> saveAll(Collection<T> entities);

  /**
   * 更新实体
   *
   * @param entity 实体对象
   * @return 更新后的实体
   */
  T update(T entity);

  /**
   * 批量更新实体
   *
   * @param entities 实体集合
   * @return 更新后的实体列表
   */
  List<T> updateAll(Collection<T> entities);

  // ==================== 删除操作 ====================

  /**
   * 根据ID删除
   *
   * @param id 主键
   * @return 是否删除成功
   */
  boolean deleteById(K id);

  /**
   * 批量删除
   *
   * @param ids 主键集合
   * @return 删除的数量
   */
  long deleteAllById(Collection<K> ids);

  /**
   * 删除实体
   *
   * @param entity 实体对象
   * @return 是否删除成功
   */
  boolean delete(T entity);

  // ==================== 查询操作 ====================

  /**
   * 根据ID查询
   *
   * @param id 主键
   * @return 实体（可能为空）
   */
  Optional<T> findById(K id);

  /**
   * 根据ID批量查询
   *
   * @param ids 主键集合
   * @return 实体列表
   */
  List<T> findAllById(Collection<K> ids);

  /**
   * 查询所有
   *
   * @return 实体列表
   */
  List<T> findAll();

  /**
   * 分页查询
   *
   * @param pageQuery 分页参数
   * @return 分页结果
   */
  PageResult<T> findAll(PageQuery pageQuery);

  // ==================== 统计操作 ====================

  /**
   * 统计总数
   *
   * @return 总数
   */
  long count();

  /**
   * 判断是否存在
   *
   * @param id 主键
   * @return 是否存在
   */
  boolean existsById(K id);
}
