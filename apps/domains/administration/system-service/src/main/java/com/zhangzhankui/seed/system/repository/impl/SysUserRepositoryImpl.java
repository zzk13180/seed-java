package com.zhangzhankui.seed.system.repository.impl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangzhankui.seed.common.core.domain.PageQuery;
import com.zhangzhankui.seed.common.core.domain.PageResult;
import com.zhangzhankui.seed.common.datasource.repository.MybatisPlusRepository;
import com.zhangzhankui.seed.system.api.vo.SysUserQueryVO;
import com.zhangzhankui.seed.system.domain.SysUser;
import com.zhangzhankui.seed.system.mapper.SysUserMapper;
import com.zhangzhankui.seed.system.repository.SysUserRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户仓储实现
 *
 * <p>基于 MyBatis Plus 实现用户数据访问
 */
@Repository
public class SysUserRepositoryImpl
    extends MybatisPlusRepository<SysUserMapper, SysUser, Long>
    implements SysUserRepository {

  @Override
  protected Long getId(SysUser entity) {
    return entity.getUserId();
  }

  @Override
  protected void setId(SysUser entity, Long id) {
    entity.setUserId(id);
  }

  @Override
  public Optional<SysUser> findByUsername(String username) {
    return Optional.ofNullable(mapper.selectByUsername(username));
  }

  @Override
  public boolean existsByUsername(String username) {
    LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SysUser::getUsername, username);
    return mapper.exists(wrapper);
  }

  @Override
  public Set<String> findRoleKeysByUserId(Long userId) {
    Set<String> roles = mapper.selectRoleKeysByUserId(userId);
    return roles != null ? roles : new HashSet<>();
  }

  @Override
  public Set<String> findPermissionsByUserId(Long userId) {
    Set<String> permissions = mapper.selectPermsByUserId(userId);
    return permissions != null ? permissions : new HashSet<>();
  }

  @Override
  public boolean updatePassword(Long userId, String password) {
    LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SysUser::getUserId, userId);

    SysUser update = new SysUser();
    update.setPassword(password);

    return mapper.update(update, wrapper) > 0;
  }

  @Override
  public void softDeleteByIds(Iterable<Long> userIds) {
    LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
    wrapper.in(SysUser::getUserId, userIds);

    SysUser update = new SysUser();
    update.setDeleted(1);

    mapper.update(update, wrapper);
  }

  @Override
  public boolean updateStatus(Long userId, Integer status) {
    LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SysUser::getUserId, userId);

    SysUser update = new SysUser();
    update.setStatus(status);

    return mapper.update(update, wrapper) > 0;
  }

  @Override
  public PageResult<SysUser> findByCondition(SysUserQueryVO query, PageQuery pageQuery) {
    Page<SysUser> page = createPage(pageQuery);

    LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
    wrapper.select(
            SysUser::getUserId,
            SysUser::getUsername,
            SysUser::getNickname,
            SysUser::getEmail,
            SysUser::getPhone,
            SysUser::getAvatar,
            SysUser::getStatus,
            SysUser::getDeptId,
            SysUser::getCreateTime
        )
        .like(query.username() != null, SysUser::getUsername, query.username())
        .like(query.nickname() != null, SysUser::getNickname, query.nickname())
        .eq(query.status() != null, SysUser::getStatus, query.status())
        .eq(query.deptId() != null, SysUser::getDeptId, query.deptId())
        .orderByDesc(SysUser::getCreateTime);

    Page<SysUser> result = mapper.selectPage(page, wrapper);
    return toPageResult(result);
  }
}
