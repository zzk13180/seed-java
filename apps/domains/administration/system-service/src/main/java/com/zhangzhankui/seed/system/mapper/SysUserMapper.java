package com.zhangzhankui.seed.system.mapper;

import java.util.Set;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangzhankui.seed.system.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 用户Mapper */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

  /** 根据用户名查询用户 */
  @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
  SysUser selectByUsername(@Param("username") String username);

  /** 查询用户角色Key列表 */
  @Select(
      """
      SELECT r.role_key FROM sys_role r
      INNER JOIN sys_user_role ur ON r.role_id = ur.role_id
      WHERE ur.user_id = #{userId} AND r.status = 1 AND r.deleted = 0
      """)
  Set<String> selectRoleKeysByUserId(@Param("userId") Long userId);

  /** 查询用户权限列表 */
  @Select(
      """
      SELECT DISTINCT m.perms FROM sys_menu m
      INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id
      INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
      WHERE ur.user_id = #{userId} AND m.status = 1 AND m.perms IS NOT NULL AND m.perms != ''
      """)
  Set<String> selectPermsByUserId(@Param("userId") Long userId);
}
