package com.zhangzhankui.samples.controller;

import com.zhangzhankui.samples.api.UserService;
import com.zhangzhankui.samples.api.dto.ResetPasswordDTO;
import com.zhangzhankui.samples.api.dto.UserCreateDTO;
import com.zhangzhankui.samples.api.dto.UserQueryDTO;
import com.zhangzhankui.samples.api.dto.UserUpdateDTO;
import com.zhangzhankui.samples.api.vo.UserVO;
import com.zhangzhankui.samples.common.core.controller.ResponseMessage;
import com.zhangzhankui.samples.common.core.domain.PageResult;
import com.zhangzhankui.samples.common.core.enums.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理", description = "用户增删改查相关接口")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "分页查询用户")
    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseMessage<PageResult<UserVO>> page(@Valid UserQueryDTO query) {
        return ResponseMessage.ok(userService.page(query));
    }

    @Operation(summary = "查询所有用户")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseMessage<List<UserVO>> list() {
        return ResponseMessage.ok(userService.findAll());
    }

    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseMessage<UserVO> getById(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        return ResponseMessage.ok(userService.findById(id));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseMessage<UserVO> create(@Valid @RequestBody UserCreateDTO dto) {
        return ResponseMessage.ok(userService.create(dto));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseMessage<UserVO> update(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO dto) {
        return ResponseMessage.ok(userService.update(id, dto));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete') or hasRole('ADMIN')")
    public ResponseMessage<Void> delete(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        userService.delete(id);
        return ResponseMessage.ok();
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseMessage<Void> deleteBatch(@RequestBody List<Long> ids) {
        userService.deleteBatch(ids);
        return ResponseMessage.ok();
    }

    @Operation(summary = "更新用户状态")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseMessage<Void> updateStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "状态: 0-禁用 1-启用") @RequestParam Integer status) {
        userService.updateStatus(id, UserStatus.fromValue(status));
        return ResponseMessage.ok();
    }

    @Operation(summary = "重置用户密码")
    @PatchMapping("/{id}/password/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseMessage<Void> resetPassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(id, dto);
        return ResponseMessage.ok();
    }

    @Operation(summary = "检查用户名是否存在")
    @GetMapping("/exists/username")
    public ResponseMessage<Boolean> existsByUsername(
            @Parameter(description = "用户名") @RequestParam String username) {
        return ResponseMessage.ok(userService.existsByUsername(username));
    }

    @Operation(summary = "检查邮箱是否存在")
    @GetMapping("/exists/email")
    public ResponseMessage<Boolean> existsByEmail(
            @Parameter(description = "邮箱") @RequestParam String email) {
        return ResponseMessage.ok(userService.existsByEmail(email));
    }
}
