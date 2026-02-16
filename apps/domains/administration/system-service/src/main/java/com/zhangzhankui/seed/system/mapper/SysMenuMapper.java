package com.zhangzhankui.seed.system.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangzhankui.seed.system.domain.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 菜单Mapper */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

  /** 查询用户菜单列表 */
  @Select(
      """
      SELECT DISTINCT m.* FROM sys_menu m
      INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id
      INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
      WHERE ur.user_id = #{userId} AND m.status = 1 AND m.menu_type IN ('M', 'C')
      ORDER BY m.parent_id, m.sort
      """)
  List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
}
