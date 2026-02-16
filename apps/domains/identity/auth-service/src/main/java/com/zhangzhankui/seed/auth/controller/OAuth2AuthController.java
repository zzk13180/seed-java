package com.zhangzhankui.seed.auth.controller;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.zhangzhankui.seed.auth.service.UserSyncService;
import com.zhangzhankui.seed.common.core.domain.ApiResult;
import com.zhangzhankui.seed.common.redis.utils.RedisUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth2/OIDC 认证控制器
 *
 * <p>当 seed.auth.provider=oauth2 时激活
 *
 * <p>登录流程：
 *
 * <ol>
 *   <li>前端跳转到 /auth/login → Spring Security 自动重定向到 OIDC Provider
 *   <li>用户在 OIDC Provider 完成登录
 *   <li>Provider 回调 /auth/callback → 本控制器处理
 *   <li>同步用户信息到 system 服务
 *   <li>重定向到前端，携带 access_token
 * </ol>
 */
@Tag(name = "OAuth2 认证管理", description = "OAuth2/OIDC 认证，支持 Logto、Keycloak 等")
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "seed.auth.provider", havingValue = "oauth2")
public class OAuth2AuthController {

  private final UserSyncService userSyncService;
  private final RedisUtils redisUtils;

  private static final String AUTH_CODE_PREFIX = "oauth2:auth_code:";
  private static final long AUTH_CODE_TTL_SECONDS = 60;

  @Value("${seed.auth.oauth2.frontend-redirect-uri:http://localhost:3000/auth/callback}")
  private String frontendRedirectUri;

  /**
   * OAuth2 登录成功回调
   *
   * <p>Spring Security OAuth2 Client 会自动处理授权码流程， 这个端点用于登录成功后的用户同步和前端重定向
   */
  @Operation(summary = "OAuth2 登录成功回调", description = "登录成功后同步用户信息并重定向到前端")
  @GetMapping("/callback")
  public ResponseEntity<Void> loginSuccess(@AuthenticationPrincipal OidcUser oidcUser) {
    if (oidcUser == null) {
      log.warn("OAuth2 回调但无用户信息");
      return ResponseEntity.status(HttpStatus.FOUND)
          .location(URI.create(frontendRedirectUri + "?error=no_user"))
          .build();
    }

    // 同步用户信息到 system 服务
    userSyncService.syncUser(oidcUser);

    // 生成一次性 authorization code，避免在 URL 中传递 Token
    String accessToken = oidcUser.getIdToken().getTokenValue();
    String authCode = UUID.randomUUID().toString();
    redisUtils.set(AUTH_CODE_PREFIX + authCode, accessToken, AUTH_CODE_TTL_SECONDS, TimeUnit.SECONDS);

    log.info("用户 {} OAuth2 登录成功", oidcUser.getSubject());

    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(frontendRedirectUri + "?code=" + authCode))
        .build();
  }

  /** 获取当前 OAuth2 用户信息 */
  @Operation(summary = "获取 OAuth2 用户信息")
  @GetMapping("/info")
  public ApiResult<OidcUserInfo> getUserInfo(@AuthenticationPrincipal OidcUser oidcUser) {
    if (oidcUser == null) {
      return ApiResult.unauthorized();
    }

    OidcUserInfo userInfo =
        new OidcUserInfo(
            oidcUser.getSubject(),
            oidcUser.getPreferredUsername(),
            oidcUser.getEmail(),
            oidcUser.getFullName(),
            oidcUser.getPicture());

    return ApiResult.ok(userInfo);
  }

  /** 用一次性 code 换取 Token */
  @Operation(summary = "OAuth2 Code 换取 Token", description = "前端使用一次性 code 换取 access_token")
  @GetMapping("/exchange")
  public ApiResult<String> exchangeToken(@RequestParam String code) {
    String key = AUTH_CODE_PREFIX + code;
    // 使用原子 GETDEL 操作，防止并发重放攻击
    Object token = redisUtils.getAndDelete(key);
    if (token == null) {
      return ApiResult.unauthorized();
    }
    return ApiResult.ok(token.toString());
  }

  /** OAuth2 用户信息 DTO */
  public record OidcUserInfo(
      String sub, String username, String email, String name, String picture) {}
}
