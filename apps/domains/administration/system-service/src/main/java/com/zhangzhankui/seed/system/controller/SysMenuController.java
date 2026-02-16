package com.zhangzhankui.seed.system.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhangzhankui.seed.common.core.domain.ApiResult;
import com.zhangzhankui.seed.common.security.utils.SecurityUtils;
import com.zhangzhankui.seed.system.domain.SysMenu;
import com.zhangzhankui.seed.system.mapper.SysMenuMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜单管理 Controller
 *
 * <p>提供菜单树查询等接口（最小实现）
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

  private final SysMenuMapper menuMapper;

  /**
   * 获取当前用户的菜单树
   *
   * <p>前端侧边栏使用此接口加载导航菜单
   */
  @Operation(summary = "获取当前用户菜单树")
  @GetMapping("/user")
  public ApiResult<List<MenuTreeVO>> userMenuTree() {
    Long userId = SecurityUtils.getUserId();

    List<SysMenu> menus;
    // 超级管理员（userId=1）查看所有菜单
    if (userId != null && userId == 1L) {
      menus = menuMapper.selectList(
          new LambdaQueryWrapper<SysMenu>()
              .in(SysMenu::getMenuType, "M", "C")
              .eq(SysMenu::getStatus, 1)
              .orderByAsc(SysMenu::getParentId)
              .orderByAsc(SysMenu::getSort));
    } else {
      menus = menuMapper.selectMenusByUserId(userId);
    }

    return ApiResult.ok(buildMenuTree(menus, 0L));
  }

  /**
   * 获取完整菜单树（管理页面使用）
   */
  @Operation(summary = "获取完整菜单树")
  @GetMapping("/tree")
  public ApiResult<List<MenuTreeVO>> menuTree() {
    List<SysMenu> menus = menuMapper.selectList(
        new LambdaQueryWrapper<SysMenu>()
            .eq(SysMenu::getStatus, 1)
            .orderByAsc(SysMenu::getParentId)
            .orderByAsc(SysMenu::getSort));
    return ApiResult.ok(buildMenuTree(menus, 0L));
  }

  /** 递归构建菜单树 */
  private List<MenuTreeVO> buildMenuTree(List<SysMenu> menus, Long parentId) {
    Map<Long, List<SysMenu>> grouped = menus.stream()
        .collect(Collectors.groupingBy(SysMenu::getParentId));

    return buildChildren(grouped, parentId);
  }

  private List<MenuTreeVO> buildChildren(Map<Long, List<SysMenu>> grouped, Long parentId) {
    List<SysMenu> children = grouped.getOrDefault(parentId, List.of());
    List<MenuTreeVO> result = new ArrayList<>(children.size());
    for (SysMenu menu : children) {
      MenuTreeVO vo = new MenuTreeVO();
      vo.path = menu.getPath();
      vo.title = menu.getMenuName();
      vo.icon = menu.getIcon();
      List<MenuTreeVO> subChildren = buildChildren(grouped, menu.getMenuId());
      if (!subChildren.isEmpty()) {
        vo.children = subChildren;
      }
      result.add(vo);
    }
    return result;
  }

  /** 菜单树 VO（匹配前端 MenuItem 类型） */
  public static class MenuTreeVO {
    public String path;
    public String title;
    public String icon;
    public List<MenuTreeVO> children;
  }
}
