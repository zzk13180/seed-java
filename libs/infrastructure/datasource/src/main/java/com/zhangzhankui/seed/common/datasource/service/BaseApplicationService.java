package com.zhangzhankui.seed.common.datasource.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.zhangzhankui.seed.common.core.domain.PageQuery;
import com.zhangzhankui.seed.common.core.domain.PageResult;
import com.zhangzhankui.seed.common.core.exception.ServiceException;
import com.zhangzhankui.seed.common.core.repository.IRepository;
import com.zhangzhankui.seed.common.core.service.IBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * 应用服务基类
 *
 * <p>实现 {@link IBaseService} 接口，提供基础 CRUD 操作的默认实现。
 * 此类组合使用 {@link IRepository}，遵循依赖倒置原则。
 *
 * <p>架构说明：
 * <pre>
 * 应用层 (Application Layer)
 * └── BaseApplicationService implements IBaseService
 *     └── 依赖 IRepository (通过构造器注入)
 *
 * 基础设施层 (Infrastructure Layer)
 * └── MybatisPlusRepository implements IRepository
 * </pre>
 *
 * <p>子类使用示例：
 * <pre>
 * {@code
 * @Service
 * public class UserService extends BaseApplicationService<User, Long> {
 *
 *     public UserService(UserRepository repository) {
 *         super(repository);
 *     }
 *
 *     // 自定义业务方法
 *     public User findByUsername(String username) {
 *         return ((UserRepository) getRepository()).findByUsername(username);
 *     }
 * }
 * }
 * </pre>
 *
 * @param <T> 实体类型
 * @param <K> 主键类型
 */
@Slf4j
public abstract class BaseApplicationService<T, K extends Serializable>
    implements IBaseService<T, K> {

  private final IRepository<T, K> repository;

  /**
   * 构造函数，注入仓储
   *
   * @param repository 仓储实例
   */
  protected BaseApplicationService(IRepository<T, K> repository) {
    this.repository = repository;
  }

  /**
   * 获取仓储实例
   *
   * @return 仓储
   */
  protected IRepository<T, K> getRepository() {
    return repository;
  }

  /**
   * 获取实体名称（用于异常消息）
   * 子类可重写以提供更友好的名称
   *
   * @return 实体名称
   */
  protected String getEntityName() {
    return "实体";
  }

  // ==================== 创建操作 ====================

  @Override
  @Transactional
  public T create(T entity) {
    log.debug("创建{}: {}", getEntityName(), entity);
    return repository.save(entity);
  }

  @Override
  @Transactional
  public List<T> createAll(Collection<T> entities) {
    if (entities == null || entities.isEmpty()) {
      return List.of();
    }
    log.debug("批量创建{}, 数量: {}", getEntityName(), entities.size());
    return repository.saveAll(entities);
  }

  // ==================== 更新操作 ====================

  @Override
  @Transactional
  public T update(T entity) {
    log.debug("更新{}: {}", getEntityName(), entity);
    return repository.update(entity);
  }

  @Override
  @Transactional
  public List<T> updateAll(Collection<T> entities) {
    if (entities == null || entities.isEmpty()) {
      return List.of();
    }
    log.debug("批量更新{}, 数量: {}", getEntityName(), entities.size());
    return repository.updateAll(entities);
  }

  // ==================== 删除操作 ====================

  @Override
  @Transactional
  public void deleteById(K id) {
    log.debug("删除{}, ID: {}", getEntityName(), id);
    boolean deleted = repository.deleteById(id);
    if (!deleted) {
      log.warn("删除{}失败, ID: {} 不存在", getEntityName(), id);
    }
  }

  @Override
  @Transactional
  public void deleteAllById(Collection<K> ids) {
    if (ids == null || ids.isEmpty()) {
      return;
    }
    log.debug("批量删除{}, IDs: {}", getEntityName(), ids);
    repository.deleteAllById(ids);
  }

  // ==================== 查询操作 ====================

  @Override
  public Optional<T> findById(K id) {
    return repository.findById(id);
  }

  @Override
  public T getById(K id) {
    return repository.findById(id)
        .orElseThrow(() -> new ServiceException(getEntityName() + "不存在, ID: " + id));
  }

  @Override
  public List<T> findAllById(Collection<K> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return repository.findAllById(ids);
  }

  @Override
  public List<T> findAll() {
    return repository.findAll();
  }

  @Override
  public PageResult<T> findAll(PageQuery pageQuery) {
    return repository.findAll(pageQuery);
  }

  // ==================== 统计操作 ====================

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public boolean existsById(K id) {
    return repository.existsById(id);
  }
}
