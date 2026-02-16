package com.zhangzhankui.seed.system.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.zhangzhankui.seed.common.core.constant.CacheConstants;
import com.zhangzhankui.seed.common.core.constant.SecurityConstants;
import com.zhangzhankui.seed.common.core.domain.LoginUser;
import com.zhangzhankui.seed.common.core.domain.PageQuery;
import com.zhangzhankui.seed.common.core.domain.PageResult;
import com.zhangzhankui.seed.common.core.exception.ServiceException;
import com.zhangzhankui.seed.common.datasource.service.BaseApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zhangzhankui.seed.system.api.dto.SysUserDTO;
import com.zhangzhankui.seed.system.api.dto.UserCredentialsDTO;
import com.zhangzhankui.seed.system.api.vo.SysUserQueryVO;
import com.zhangzhankui.seed.system.converter.SysUserConverter;
import com.zhangzhankui.seed.system.domain.SysUser;
import com.zhangzhankui.seed.system.repository.SysUserRepository;
import com.zhangzhankui.seed.system.service.SysUserService;

/**
 * 用户服务实现
 *
 * <p>继承 BaseApplicationService，组合使用 SysUserRepository
 */
@Slf4j
@Service
@CacheConfig(cacheNames = CacheConstants.USER_LOGIN_KEY)
public class SysUserServiceImpl
    extends BaseApplicationService<SysUser, Long>
    implements SysUserService {

  private final SysUserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public SysUserServiceImpl(SysUserRepository userRepository, PasswordEncoder passwordEncoder) {
    super(userRepository);
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  protected String getEntityName() {
    return "用户";
  }

  @Override
  public SysUser findByUsername(String username) {
    return userRepository.findByUsername(username).orElse(null);
  }

  @Override
  public boolean existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }

  /**
   * 加载用户的角色和权限集合
   *
   * @param userId 用户ID
   * @return 角色集合与权限集合的记录
   */
  private RolesAndPermissions loadRolesAndPermissions(Long userId) {
    Set<String> roles = new HashSet<>(userRepository.findRoleKeysByUserId(userId));
    Set<String> permissions = new HashSet<>(userRepository.findPermissionsByUserId(userId));

    // 管理员拥有所有权限
    if (roles.contains(SecurityConstants.ROLE_ADMIN)) {
      permissions.add(SecurityConstants.ALL_PERMISSIONS);
    }
    return new RolesAndPermissions(roles, permissions);
  }

  private record RolesAndPermissions(Set<String> roles, Set<String> permissions) {}

  @Override
  public UserCredentialsDTO getUserCredentials(String username) {
    SysUser user = findByUsername(username);
    if (user == null) {
      log.debug("获取凭据失败: 用户 {} 不存在", username);
      return null;
    }

    RolesAndPermissions rp = loadRolesAndPermissions(user.getUserId());

    return new UserCredentialsDTO(
        user.getUserId(),
        user.getUsername(),
        user.getNickname(),
        user.getPassword(),
        user.getStatus(),
        user.getDeptId(),
        rp.roles(),
        rp.permissions());
  }

  @Cacheable(key = "#username", unless = "#result == null")
  @Override
  public LoginUser getLoginUserByUsername(String username) {
    SysUser user = findByUsername(username);
    if (user == null) {
      return null;
    }

    RolesAndPermissions rp = loadRolesAndPermissions(user.getUserId());

    LoginUser loginUser = new LoginUser();
    loginUser.setUserId(user.getUserId());
    loginUser.setDeptId(user.getDeptId());
    loginUser.setUsername(user.getUsername());
    loginUser.setNickname(user.getNickname());
    loginUser.setEmail(user.getEmail());
    loginUser.setPhone(user.getPhone());
    loginUser.setAvatar(user.getAvatar());
    loginUser.setStatus(user.getStatus());
    loginUser.setRoles(rp.roles());
    loginUser.setPermissions(rp.permissions());
    // 不将密码哈希存入缓存，防止通过 Redis 泄露
    loginUser.setPassword(null);

    return loginUser;
  }

  @Override
  public LoginUser getLoginUserByUserId(Long userId) {
    SysUser user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      return null;
    }
    return getLoginUserByUsername(user.getUsername());
  }

  @CacheEvict(key = "#username")
  @Transactional
  @Override
  public boolean updatePassword(Long userId, String username, String password) {
    if (!userRepository.existsById(userId)) {
      log.warn("更新密码失败，用户不存在: {}", userId);
      return false;
    }
    return userRepository.updatePassword(userId, password);
  }

  @Transactional
  @Override
  public void softDelete(List<Long> userIds) {
    log.info("逻辑删除用户: {}", userIds);
    userRepository.softDeleteByIds(userIds);
  }

  @Transactional
  @Override
  public void changeStatus(Long userId, Integer status) {
    log.info("修改用户状态: userId={}, status={}", userId, status);
    userRepository.updateStatus(userId, status);
  }

  @Override
  public PageResult<SysUser> queryUserPage(SysUserQueryVO query, PageQuery pageQuery) {
    return userRepository.findByCondition(query, pageQuery);
  }

  @Transactional
  @Override
  public void createUser(SysUserDTO dto, PasswordEncoder passwordEncoder) {
    // 唯一性检查和创建在同一事务中，配合数据库唯一索引，避免 TOCTOU 竞态
    if (existsByUsername(dto.username())) {
      throw new ServiceException("用户名已存在");
    }

    // 新增用户时密码必填
    if (dto.password() == null || dto.password().isBlank()) {
      throw new ServiceException("新增用户时密码不能为空");
    }

    SysUser user = SysUserConverter.INSTANCE.toEntity(dto);

    // BCrypt 密码加密
    user.setPassword(passwordEncoder.encode(dto.password()));

    try {
      create(user);
    } catch (DataIntegrityViolationException e) {
      throw new ServiceException("用户名已存在");
    }
    log.info("创建用户成功: {}", dto.username());
  }

  @Transactional
  @Override
  public void createOAuth2User(SysUserDTO dto, PasswordEncoder passwordEncoder) {
    // 唯一性检查
    if (existsByUsername(dto.username())) {
      log.debug("OAuth2 用户 {} 已存在，跳过创建", dto.username());
      return;
    }

    SysUser user = SysUserConverter.INSTANCE.toEntity(dto);
    // OAuth2 用户生成随机强密码（用户不需要使用密码登录）
    String randomPassword = UUID.randomUUID().toString() + UUID.randomUUID().toString();
    user.setPassword(passwordEncoder.encode(randomPassword));

    try {
      create(user);
    } catch (DataIntegrityViolationException e) {
      throw new ServiceException("用户名已存在");
    }
    log.info("创建 OAuth2 用户成功: {}", dto.username());
  }
}
