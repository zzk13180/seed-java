package com.zhangzhankui.seed.system.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.zhangzhankui.seed.common.core.annotation.InnerAuth;
import com.zhangzhankui.seed.common.core.annotation.Log;
import com.zhangzhankui.seed.common.core.annotation.Log.BusinessType;
import com.zhangzhankui.seed.common.core.domain.ApiResult;
import com.zhangzhankui.seed.common.core.domain.LoginUser;
import com.zhangzhankui.seed.common.core.domain.PageQuery;
import com.zhangzhankui.seed.common.core.domain.PageResult;
import com.zhangzhankui.seed.system.api.dto.ChangeStatusDTO;
import com.zhangzhankui.seed.system.api.dto.ResetPasswordDTO;
import com.zhangzhankui.seed.system.api.dto.SysUserDTO;
import com.zhangzhankui.seed.system.api.dto.UserCredentialsDTO;
import com.zhangzhankui.seed.system.api.vo.SysUserQueryVO;
import com.zhangzhankui.seed.system.api.vo.SysUserVO;
import com.zhangzhankui.seed.system.converter.SysUserConverter;
import com.zhangzhankui.seed.system.domain.SysDept;
import com.zhangzhankui.seed.system.domain.SysUser;
import com.zhangzhankui.seed.system.mapper.SysDeptMapper;
import com.zhangzhankui.seed.system.service.SysUserService;
import com.zhangzhankui.seed.common.web.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController extends BaseController {

  private final SysUserService userService;
  private final PasswordEncoder passwordEncoder;
  private final SysDeptMapper deptMapper;
  private static final SysUserConverter CONVERTER = SysUserConverter.INSTANCE;

  @InnerAuth
  @Operation(summary = "通过用户名获取用户信息（内部调用）")
  @GetMapping("/info/{username}")
  public ApiResult<LoginUser> info(@PathVariable String username) {
    LoginUser loginUser = userService.getLoginUserByUsername(username);
    return ApiResult.ok(loginUser);
  }

  @InnerAuth
  @Operation(summary = "通过用户ID获取用户信息（内部调用）")
  @GetMapping("/info/id/{userId}")
  public ApiResult<LoginUser> infoById(@PathVariable Long userId) {
    LoginUser loginUser = userService.getLoginUserByUserId(userId);
    return ApiResult.ok(loginUser);
  }

  @InnerAuth
  @Operation(summary = "获取用户凭据信息（内部调用，含密码哈希）")
  @GetMapping("/credentials/{username}")
  public ApiResult<UserCredentialsDTO> getUserCredentials(@PathVariable String username) {
    UserCredentialsDTO credentials = userService.getUserCredentials(username);
    if (credentials == null) {
      return ApiResult.fail("用户不存在");
    }
    return ApiResult.ok(credentials);
  }

  @Operation(summary = "用户列表")
  @GetMapping("/list")
  public ApiResult<PageResult<SysUserVO>> list(SysUserQueryVO query, PageQuery pageQuery) {
    PageResult<SysUser> result = userService.queryUserPage(query, pageQuery);

    // 收集所有 deptId，批量查询部门名称
    Set<Long> deptIds = result.getRecords().stream()
        .map(SysUser::getDeptId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Map<Long, SysDept> deptMap = Map.of();
    if (!deptIds.isEmpty()) {
      deptMap = deptMapper.selectBatchIds(deptIds).stream()
          .collect(Collectors.toMap(SysDept::getDeptId, d -> d, (a, b) -> a));
    }
    final Map<Long, SysDept> finalDeptMap = deptMap;

    // 转换为 VO，填充 deptName
    PageResult<SysUserVO> voPage = result.map(user -> {
      SysDept dept = user.getDeptId() != null ? finalDeptMap.get(user.getDeptId()) : null;
      return CONVERTER.toVO(user, dept);
    });

    return ApiResult.ok(voPage);
  }

  @Operation(summary = "用户详情")
  @GetMapping("/{userId}")
  public ApiResult<SysUserVO> getInfo(@PathVariable Long userId) {
    SysUser user = userService.getById(userId);
    return ApiResult.ok(CONVERTER.toVO(user));
  }

  @Log(title = "用户管理", businessType = BusinessType.INSERT)
  @Operation(summary = "新增用户")
  @PostMapping
  public ApiResult<Void> add(@Validated @RequestBody SysUserDTO dto) {
    // 唯一性校验 + 创建在同一 Service 事务中完成，避免 TOCTOU 竞态
    userService.createUser(dto, passwordEncoder);
    return ApiResult.ok();
  }

  @InnerAuth
  @Operation(summary = "创建 OAuth2 用户（内部调用）")
  @PostMapping("/oauth2")
  public ApiResult<Void> addOAuth2User(@RequestBody SysUserDTO dto) {
    userService.createOAuth2User(dto, passwordEncoder);
    return ApiResult.ok();
  }

  @Log(title = "用户管理", businessType = BusinessType.UPDATE)
  @Operation(summary = "修改用户")
  @PutMapping
  public ApiResult<Void> edit(@Validated @RequestBody SysUserDTO dto) {
    // 使用 MapStruct 转换 DTO 到 Entity
    SysUser user = CONVERTER.toEntity(dto);
    // 更新时不更新密码字段
    user.setPassword(null);
    userService.update(user);
    return ApiResult.ok();
  }

  @Log(title = "用户管理", businessType = BusinessType.DELETE)
  @Operation(summary = "删除用户")
  @DeleteMapping("/{userIds}")
  public ApiResult<Void> remove(@PathVariable List<Long> userIds) {
    // 校验：禁止空列表
    if (userIds == null || userIds.isEmpty()) {
      return ApiResult.badRequest("请选择要删除的用户");
    }
    // 校验：数量限制
    if (userIds.size() > 100) {
      return ApiResult.badRequest("每次最多删除100个用户");
    }
    // 校验：禁止删除自己
    Long currentUserId = getCurrentUserId();
    if (currentUserId != null && userIds.contains(currentUserId)) {
      return ApiResult.badRequest("不能删除当前登录用户");
    }
    // 校验：禁止删除管理员（userId=1 为超级管理员）
    if (userIds.contains(1L)) {
      return ApiResult.badRequest("不能删除超级管理员");
    }
    userService.softDelete(userIds);
    return ApiResult.ok();
  }

  @Log(title = "用户管理", businessType = BusinessType.UPDATE)
  @Operation(summary = "重置密码")
  @PutMapping("/resetPwd")
  public ApiResult<Void> resetPwd(@Validated @RequestBody ResetPasswordDTO dto) {
    // 查询用户名用于缓存清除
    SysUser existingUser = userService.getById(dto.userId());
    if (existingUser == null) {
      return ApiResult.badRequest("用户不存在");
    }
    // BCrypt 密码加密
    String encodedPassword = passwordEncoder.encode(dto.password());
    boolean success = userService.updatePassword(dto.userId(), existingUser.getUsername(), encodedPassword);
    if (!success) {
      return ApiResult.badRequest("用户不存在");
    }
    return ApiResult.ok();
  }

  @Log(title = "用户管理", businessType = BusinessType.UPDATE)
  @Operation(summary = "状态修改")
  @PutMapping("/changeStatus")
  public ApiResult<Void> changeStatus(@Validated @RequestBody ChangeStatusDTO dto) {
    userService.changeStatus(dto.userId(), dto.status());
    return ApiResult.ok();
  }
}
