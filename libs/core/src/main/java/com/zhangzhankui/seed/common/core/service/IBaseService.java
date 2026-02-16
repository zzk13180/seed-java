package com.zhangzhankui.seed.common.core.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.zhangzhankui.seed.common.core.domain.PageQuery;
import com.zhangzhankui.seed.common.core.domain.PageResult;

/**
 * 应用服务基础接口
 *
 * <p>定义应用层通用的 CRUD 操作。与仓储层的区别：
 * <ul>
 *   <li>IRepository - 领域层，纯数据访问
 *   <li>IBaseService - 应用层，包含业务逻辑、事务、缓存等
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>单一职责 - 每个方法只做一件事
 *   <li>命名一致 - 使用领域语义（save、find、delete）
 *   <li>框架无关 - 不引入任何框架特定类型
 * </ul>
 *
 * @param <T> 实体类型
 * @param <K> 主键类型
 */
public interface IBaseService<T, K extends Serializable> {

  // ==================== 创建操作 ====================

  /**
   * 创建实体
   *
   * @param entity 实体对象
   * @return 创建后的实体
   */
  T create(T entity);

  /**
   * 批量创建实体
   *
   * @param entities 实体集合
   * @return 创建后的实体列表
   */
  List<T> createAll(Collection<T> entities);

  // ==================== 更新操作 ====================

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
   */
  void deleteById(K id);

  /**
   * 批量删除
   *
   * @param ids 主键集合
   */
  void deleteAllById(Collection<K> ids);

  // ==================== 查询操作 ====================

  /**
   * 根据ID查询
   *
   * @param id 主键
   * @return 实体（可能为空）
   */
  Optional<T> findById(K id);

  /**
   * 根据ID查询（不存在则抛异常）
   *
   * @param id 主键
   * @return 实体
   * @throws com.zhangzhankui.seed.common.core.exception.ServiceException 实体不存在时
   */
  T getById(K id);

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
