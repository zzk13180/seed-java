package com.zhangzhankui.seed.auth.service;

import com.zhangzhankui.seed.system.api.RemoteUserService;
import com.zhangzhankui.seed.system.api.dto.SysUserDTO;
import com.zhangzhankui.seed.common.core.domain.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * 用户同步服务
 *
 * <p>将 OAuth2 Provider (Logto/Keycloak/Auth0) 的用户信息同步到 system 服务
 *
 * <p>仅在 OAuth2 模式下激活
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "seed.auth.provider", havingValue = "oauth2")
public class UserSyncService {

  private final RemoteUserService remoteUserService;

  /**
   * 同步 OIDC 用户信息到系统
   *
   * @param oidcUser OIDC 用户信息
   */
  public void syncUser(OidcUser oidcUser) {
    String sub = oidcUser.getSubject();
    String username = oidcUser.getPreferredUsername();
    String email = oidcUser.getEmail();
    // String name = oidcUser.getFullName();

    log.info("同步用户信息: sub={}, username={}, email={}", sub, username, email);

    try {
      // 检查用户是否存在
      ApiResult<?> result = remoteUserService.getUserInfo(username);

      if (result.getCode() == ApiResult.FAIL) {
        // 用户不存在，创建新用户
        log.info("用户 {} 不存在，将创建新用户", username);
        SysUserDTO newUser = new SysUserDTO(
            null,       // userId
            username,   // username
            null,       // password（OAuth2 用户无需密码）
            oidcUser.getFullName(), // nickname
            email,      // email
            null,       // phone
            null,       // sex
            oidcUser.getPicture(), // avatar
            null,       // deptId
            1,          // status: 正常
            "OAuth2 同步创建" // remark
        );
        remoteUserService.createOAuth2User(newUser);
        log.info("用户 {} 创建成功", username);
      } else {
        log.debug("用户 {} 已存在，跳过同步", username);
      }
    } catch (Exception e) {
      // 同步失败不影响登录
      log.warn("用户信息同步失败: {}", e.getMessage());
    }
  }
}
