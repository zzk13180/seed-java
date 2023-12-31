package com.zhangzhankui.samples.db.repository;

import com.zhangzhankui.samples.common.core.enums.UserStatus;
import com.zhangzhankui.samples.db.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 * <p>
 * 注意：由于 User 实体使用了 @SQLRestriction("deleted = false")，
 * 所有查询方法会自动过滤已删除的记录，无需手动添加 deleted = false 条件。
 * <p>
 * 同时，User 实体使用了 @SQLDelete，调用 delete/deleteById 方法会自动执行软删除。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 根据状态查找用户
     * <p>
     * 由于 @SQLRestriction 会自动过滤已删除记录，无需手动添加 deleted 条件
     */
    List<User> findAllByStatus(UserStatus status);

    /**
     * 模糊搜索用户（分页）
     * <p>
     * 由于 @SQLRestriction 会自动过滤已删除记录，无需在 JPQL 中添加 deleted = false
     */
    @Query("SELECT u FROM User u WHERE " +
           "u.username LIKE %:keyword% OR u.nickname LIKE %:keyword% OR u.email LIKE %:keyword%")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 批量软删除用户
     * <p>
     * 注意：此方法绕过了 @SQLDelete，直接执行批量更新。
     * 对于单个删除，推荐使用 deleteById() 方法（会触发 @SQLDelete）。
     */
    @Modifying
    @Query("UPDATE User u SET u.deleted = true, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id IN :ids")
    int softDeleteByIds(@Param("ids") List<Long> ids);

    /**
     * 更新用户状态
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :status, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") UserStatus status);

    /**
     * 更新密码
     */
    @Modifying
    @Query("UPDATE User u SET u.password = :password, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
