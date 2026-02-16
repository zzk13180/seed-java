package com.zhangzhankui.seed.system.repository;

import java.util.Optional;
import java.util.Set;

import com.zhangzhankui.seed.common.core.domain.PageQuery;
import com.zhangzhankui.seed.common.core.domain.PageResult;
import com.zhangzhankui.seed.common.core.repository.IRepository;
import com.zhangzhankui.seed.system.api.vo.SysUserQueryVO;
import com.zhangzhankui.seed.system.domain.SysUser;

/**
 * 用户仓储接口
 *
 * <p>扩展基础仓储接口，定义用户领域特有的数据访问方法
 */
public interface SysUserRepository extends IRepository<SysUser, Long> {

  /**
   * 根据用户名查询用户
   *
   * @param username 用户名
   * @return 用户（可能为空）
   */
  Optional<SysUser> findByUsername(String username);

  /**
   * 检查用户名是否存在
   *
   * @param username 用户名
   * @return 是否存在
   */
  boolean existsByUsername(String username);

  /**
   * 查询用户角色Key列表
   *
   * @param userId 用户ID
   * @return 角色Key集合
   */
  Set<String> findRoleKeysByUserId(Long userId);

  /**
   * 查询用户权限列表
   *
   * @param userId 用户ID
   * @return 权限标识集合
   */
  Set<String> findPermissionsByUserId(Long userId);

  /**
   * 更新用户密码
   *
   * @param userId 用户ID
   * @param password 新密码（已加密）
   * @return 是否更新成功
   */
  boolean updatePassword(Long userId, String password);

  /**
   * 逻辑删除用户
   *
   * @param userIds 用户ID列表
   */
  void softDeleteByIds(Iterable<Long> userIds);

  /**
   * 修改用户状态
   *
   * @param userId 用户ID
   * @param status 新状态
   * @return 是否更新成功
   */
  boolean updateStatus(Long userId, Integer status);

  /**
   * 条件分页查询
   *
   * @param query 查询条件
   * @param pageQuery 分页参数
   * @return 分页结果
   */
  PageResult<SysUser> findByCondition(SysUserQueryVO query, PageQuery pageQuery);
}
