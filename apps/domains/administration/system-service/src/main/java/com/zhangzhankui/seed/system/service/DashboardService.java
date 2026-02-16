package com.zhangzhankui.seed.system.service;

import java.util.HashMap;
import java.util.Map;

import com.zhangzhankui.seed.system.mapper.SysDeptMapper;
import com.zhangzhankui.seed.system.mapper.SysMenuMapper;
import com.zhangzhankui.seed.system.mapper.SysRoleMapper;
import com.zhangzhankui.seed.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 仪表盘统计服务
 *
 * <p>封装统计逻辑，避免 Controller 直接注入 Mapper
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

  private final SysUserMapper userMapper;
  private final SysDeptMapper deptMapper;
  private final SysRoleMapper roleMapper;
  private final SysMenuMapper menuMapper;

  /**
   * 获取仪表盘统计数据
   *
   * @return 各实体数量统计（通过 @TableLogic 自动过滤已删除数据）
   */
  @Cacheable(cacheNames = "dashboard", key = "'stats'")
  public Map<String, Long> getStats() {
    Map<String, Long> stats = new HashMap<>();
    stats.put("users", userMapper.selectCount(null));
    stats.put("depts", deptMapper.selectCount(null));
    stats.put("roles", roleMapper.selectCount(null));
    stats.put("menus", menuMapper.selectCount(null));
    return stats;
  }
}
