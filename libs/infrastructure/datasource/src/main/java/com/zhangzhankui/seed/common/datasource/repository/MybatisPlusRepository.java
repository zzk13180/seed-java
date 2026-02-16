package com.zhangzhankui.seed.common.datasource.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangzhankui.seed.common.core.domain.PageQuery;
import com.zhangzhankui.seed.common.core.domain.PageResult;
import com.zhangzhankui.seed.common.core.repository.IRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * MyBatis Plus 仓储实现基类
 *
 * <p>实现 {@link IRepository} 接口，封装 MyBatis Plus 的数据访问操作。
 * 此类作为所有 MyBatis Plus 仓储的基类，提供基础 CRUD 实现。
 *
 * <p>架构说明：
 * <pre>
 * core 模块 (框架无关)
 * └── IRepository&lt;T, K&gt;  ← 领域层接口
 *
 * datasource 模块 (基础设施层)
 * └── MybatisPlusRepository&lt;M, T, K&gt; implements IRepository  ← 此类
 * </pre>
 *
 * @param <M> Mapper 类型，必须继承 BaseMapper
 * @param <T> 实体类型
 * @param <K> 主键类型
 */
@Slf4j
public abstract class MybatisPlusRepository<M extends BaseMapper<T>, T, K extends Serializable>
    implements IRepository<T, K> {

  @Autowired
  protected M mapper;

  /**
   * 获取实体的 ID（由子类实现）
   *
   * @param entity 实体
   * @return 主键 ID
   */
  protected abstract K getId(T entity);

  /**
   * 设置实体的 ID（由子类实现，用于插入后回填 ID）
   *
   * @param entity 实体
   * @param id 主键 ID
   */
  protected abstract void setId(T entity, K id);

  // ==================== 持久化操作 ====================

  @Override
  public T save(T entity) {
    mapper.insert(entity);
    return entity;
  }

  @Override
  public List<T> saveAll(Collection<T> entities) {
    if (entities == null || entities.isEmpty()) {
      return List.of();
    }
    List<T> result = new ArrayList<>(entities.size());
    for (T entity : entities) {
      mapper.insert(entity);
      result.add(entity);
    }
    return result;
  }

  @Override
  public T update(T entity) {
    mapper.updateById(entity);
    return entity;
  }

  @Override
  public List<T> updateAll(Collection<T> entities) {
    if (entities == null || entities.isEmpty()) {
      return List.of();
    }
    List<T> result = new ArrayList<>(entities.size());
    for (T entity : entities) {
      mapper.updateById(entity);
      result.add(entity);
    }
    return result;
  }

  // ==================== 删除操作 ====================

  @Override
  public boolean deleteById(K id) {
    return mapper.deleteById(id) > 0;
  }

  @Override
  public long deleteAllById(Collection<K> ids) {
    if (ids == null || ids.isEmpty()) {
      return 0;
    }
    return mapper.deleteBatchIds(ids);
  }

  @Override
  public boolean delete(T entity) {
    K id = getId(entity);
    return id != null && deleteById(id);
  }

  // ==================== 查询操作 ====================

  @Override
  public Optional<T> findById(K id) {
    return Optional.ofNullable(mapper.selectById(id));
  }

  @Override
  public List<T> findAllById(Collection<K> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return mapper.selectBatchIds(ids);
  }

  @Override
  public List<T> findAll() {
    return mapper.selectList(null);
  }

  @Override
  public PageResult<T> findAll(PageQuery pageQuery) {
    Page<T> page = createPage(pageQuery);
    Page<T> result = mapper.selectPage(page, null);
    return toPageResult(result);
  }

  // ==================== 统计操作 ====================

  @Override
  public long count() {
    return mapper.selectCount(null);
  }

  @Override
  public boolean existsById(K id) {
    return mapper.selectById(id) != null;
  }

  // ==================== 辅助方法 ====================

  /**
   * 创建 MyBatis Plus 分页对象
   *
   * @param pageQuery 分页参数
   * @return MyBatis Plus Page 对象
   */
  protected Page<T> createPage(PageQuery pageQuery) {
    int pageNum = pageQuery.getPageNum() != null ? pageQuery.getPageNum() : 1;
    int pageSize = pageQuery.getPageSize() != null ? pageQuery.getPageSize() : 10;
    return new Page<>(pageNum, pageSize);
  }

  /**
   * 将 MyBatis Plus 分页结果转换为领域层 PageResult
   *
   * @param page MyBatis Plus 分页结果
   * @return 领域层分页结果
   */
  protected PageResult<T> toPageResult(Page<T> page) {
    return PageResult.of(
        page.getCurrent(),
        page.getSize(),
        page.getTotal(),
        page.getRecords()
    );
  }

  /**
   * 获取底层 Mapper（供扩展使用）
   *
   * @return Mapper 实例
   */
  protected M getMapper() {
    return mapper;
  }
}
