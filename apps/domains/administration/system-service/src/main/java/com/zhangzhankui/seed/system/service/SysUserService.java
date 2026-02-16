package com.zhangzhankui.seed.system.service;

import java.util.List;

import com.zhangzhankui.seed.common.core.domain.LoginUser;
import com.zhangzhankui.seed.common.core.domain.PageQuery;
import com.zhangzhankui.seed.common.core.domain.PageResult;
import com.zhangzhankui.seed.common.core.service.IBaseService;
import com.zhangzhankui.seed.system.api.dto.SysUserDTO;
import com.zhangzhankui.seed.system.api.dto.UserCredentialsDTO;
import com.zhangzhankui.seed.system.api.vo.SysUserQueryVO;
import com.zhangzhankui.seed.system.domain.SysUser;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 用户服务接口
 *
 * <p>扩展基础服务接口，定义用户领域特有的业务方法
 */
public interface SysUserService extends IBaseService<SysUser, Long> {

  /**
   * 根据用户名查询用户
   *
   * @param username 用户名
   * @return 用户（可能为 null）
   */
  SysUser findByUsername(String username);

  /**
   * 根据用户名获取登录用户信息
   *
   * @param username 用户名
   * @return 登录用户信息（包含角色、权限）
   */
  LoginUser getLoginUserByUsername(String username);

  /**
   * 根据用户ID获取登录用户信息
   *
   * @param userId 用户ID
   * @return 登录用户信息（包含角色、权限），用户不存在返回 null
   */
  LoginUser getLoginUserByUserId(Long userId);

  /**
   * 检查用户名是否已存在
   *
   * @param username 用户名
   * @return 是否存在
   */
  boolean existsByUsername(String username);

  /**
   * 获取用户凭据信息（含密码哈希）
   *
   * <p>仅限内部服务间调用，auth-service 使用此方法获取凭据后在本地验证密码， 避免明文密码通过 RPC 传输。
   *
   * @param username 用户名
   * @return 用户凭据信息（含 BCrypt 密码哈希、角色、权限），用户不存在返回 null
   */
  UserCredentialsDTO getUserCredentials(String username);

  /**
   * 更新用户密码
   *
   * @param userId 用户ID
   * @param username 用户名（用于清除缓存）
   * @param password 新密码（已加密）
   * @return 是否更新成功
   */
  boolean updatePassword(Long userId, String username, String password);

  /**
   * 逻辑删除用户
   *
   * @param userIds 用户ID列表
   */
  void softDelete(List<Long> userIds);

  /**
   * 修改用户状态
   *
   * @param userId 用户ID
   * @param status 新状态
   */
  void changeStatus(Long userId, Integer status);

  /**
   * 条件分页查询用户列表
   *
   * @param query 查询条件
   * @param pageQuery 分页参数
   * @return 分页结果
   */
  PageResult<SysUser> queryUserPage(SysUserQueryVO query, PageQuery pageQuery);

  /**
   * 创建用户（唯一性检查 + 创建在同一事务中，避免 TOCTOU）
   *
   * @param dto 用户DTO
   * @param passwordEncoder 密码编码器
   */
  void createUser(SysUserDTO dto, PasswordEncoder passwordEncoder);

  /**
   * 创建 OAuth2 用户（无需密码，自动生成随机强密码）
   *
   * @param dto 用户DTO（password 字段可为 null）
   * @param passwordEncoder 密码编码器
   */
  void createOAuth2User(SysUserDTO dto, PasswordEncoder passwordEncoder);
}
