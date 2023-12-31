package com.zhangzhankui.samples.controller;

import com.zhangzhankui.samples.api.AuthService;
import com.zhangzhankui.samples.api.dto.LoginDTO;
import com.zhangzhankui.samples.api.vo.LoginVO;
import com.zhangzhankui.samples.common.core.controller.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "登录、登出、刷新Token等接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ResponseMessage<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return ResponseMessage.ok(authService.login(dto));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public ResponseMessage<Void> logout() {
        authService.logout();
        return ResponseMessage.ok();
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public ResponseMessage<LoginVO> refresh(
            @Parameter(description = "刷新令牌") @RequestParam String refreshToken) {
        return ResponseMessage.ok(authService.refreshToken(refreshToken));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public ResponseMessage<LoginVO> me() {
        return ResponseMessage.ok(authService.getCurrentUser());
    }
}
