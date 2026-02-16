package com.zhangzhankui.seed.auth.controller;

import com.zhangzhankui.seed.auth.domain.vo.LoginVO;
import com.zhangzhankui.seed.auth.domain.vo.UserInfoVO;
import com.zhangzhankui.seed.auth.service.LoginService;
import com.zhangzhankui.seed.common.core.auth.AuthProvider;
import com.zhangzhankui.seed.common.core.domain.ApiResult;
import com.zhangzhankui.seed.common.core.domain.model.LoginBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 *
 * <p>安全说明： - 登录接口已内置 Rate Limiting 防暴力破解 - 登录失败次数限制，超过阈值自动锁定账户 - 使用 BCrypt 进行密码验证
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final LoginService loginService;
  private final AuthProvider authProvider;

  @Value("${sa-token.timeout:86400}")
  private long tokenTimeout;

  @Operation(summary = "用户登录", description = "内置 IP 限流和登录失败锁定机制")
  @PostMapping("/login")
  public ApiResult<LoginVO> login(@Validated @RequestBody LoginBody loginBody) {
    LoginVO result = loginService.login(loginBody.getUsername(), loginBody.getPassword());
    return ApiResult.ok(result);
  }

  @Operation(summary = "用户登出")
  @PostMapping("/logout")
  public ApiResult<Void> logout() {
    loginService.logout();
    return ApiResult.ok();
  }

  @Operation(summary = "获取用户信息")
  @GetMapping("/info")
  public ApiResult<UserInfoVO> info() {
    UserInfoVO userInfo = loginService.getUserInfo();
    return ApiResult.ok(userInfo);
  }

  @Operation(summary = "刷新Token")
  @PostMapping("/refresh")
  public ApiResult<LoginVO> refresh() {
    authProvider.refreshToken(tokenTimeout);
    String token = authProvider.getToken();
    LoginVO result = new LoginVO(token, "Authorization");
    return ApiResult.ok(result);
  }
}
