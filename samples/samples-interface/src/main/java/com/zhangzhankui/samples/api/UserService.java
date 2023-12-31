package com.zhangzhankui.samples.api;

import com.zhangzhankui.samples.api.dto.*;
import com.zhangzhankui.samples.api.vo.UserVO;
import com.zhangzhankui.samples.common.core.domain.PageResult;
import com.zhangzhankui.samples.common.core.enums.UserStatus;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 分页查询用户
     */
    PageResult<UserVO> page(UserQueryDTO query);

    /**
     * 查询所有用户
     */
    List<UserVO> findAll();

    /**
     * 根据ID查询用户
     */
    UserVO findById(Long id);

    /**
     * 根据用户名查询用户
     */
    UserVO findByUsername(String username);

    /**
     * 创建用户
     */
    UserVO create(UserCreateDTO dto);

    /**
     * 更新用户
     */
    UserVO update(Long id, UserUpdateDTO dto);

    /**
     * 删除用户 (软删除)
     * @return 被删除用户的用户名（用于缓存清理）
     */
    String delete(Long id);

    /**
     * 批量删除用户
     */
    void deleteBatch(List<Long> ids);

    /**
     * 更新用户状态
     * @return 用户名（用于缓存清理）
     */
    String updateStatus(Long id, UserStatus status);

    /**
     * 重置密码
     * @return 用户名（用于缓存清理）
     */
    String resetPassword(Long id, ResetPasswordDTO dto);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
}
