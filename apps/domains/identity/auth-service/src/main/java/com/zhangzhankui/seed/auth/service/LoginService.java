package com.zhangzhankui.seed.auth.service;

import java.util.concurrent.TimeUnit;

import com.zhangzhankui.seed.auth.config.LoginSecurityConfig;
import com.zhangzhankui.seed.auth.domain.vo.LoginVO;
import com.zhangzhankui.seed.auth.domain.vo.UserInfoVO;
import com.zhangzhankui.seed.system.api.RemoteUserService;
import com.zhangzhankui.seed.system.api.dto.UserCredentialsDTO;
import com.zhangzhankui.seed.common.core.auth.AuthProvider;
import com.zhangzhankui.seed.common.core.constant.CacheConstants;
import com.zhangzhankui.seed.common.core.domain.ApiResult;
import com.zhangzhankui.seed.common.core.domain.LoginUser;
import com.zhangzhankui.seed.common.core.exception.ServiceException;
import com.zhangzhankui.seed.common.core.exception.UserException;
import com.zhangzhankui.seed.common.core.utils.ServletUtils;
import com.zhangzhankui.seed.common.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 登录服务
 *
 * <p>现代化安全机制： - 登录失败次数限制和账户锁定 - IP 级别限流防暴力破解 - 密码在本地验证，不通过 RPC 传输明文密码 - 登录审计日志
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

  private final RemoteUserService remoteUserService;
  private final RedisUtils redisUtils;
  private final LoginSecurityConfig securityConfig;
  private final AuthProvider authProvider;
  private final PasswordEncoder passwordEncoder;

  /** 登录 */
  public LoginVO login(String username, String password) {
    // 输入验证
    if (username == null || username.isBlank()) {
      throw new ServiceException("用户名不能为空");
    }
    if (password == null || password.isBlank()) {
      throw new ServiceException("密码不能为空");
    }

    // 检查 IP 限流
    checkIpRateLimit();

    // 检查账户是否被锁定
    checkAccountLocked(username);

    // 获取用户凭据（含密码哈希），在本地验证密码，避免明文密码通过 RPC 传输
    ApiResult<UserCredentialsDTO> result = remoteUserService.getUserCredentials(username);
    if (ApiResult.FAIL == result.getCode() || result.getData() == null) {
      log.info("用户 {} 登录失败: 用户不存在", username);
      recordLoginFailure(username);
      throw new UserException("用户不存在或密码错误");
    }

    UserCredentialsDTO credentials = result.getData();

    // 在 auth-service 本地验证密码
    if (!passwordEncoder.matches(password, credentials.passwordHash())) {
      log.info("用户 {} 登录失败: 密码错误", username);
      recordLoginFailure(username);
      throw new UserException("用户不存在或密码错误");
    }

    // 构建 LoginUser（不含密码）
    LoginUser loginUser = new LoginUser();
    loginUser.setUserId(credentials.userId());
    loginUser.setUsername(credentials.username());
    loginUser.setNickname(credentials.nickname());
    loginUser.setStatus(credentials.status());
    loginUser.setDeptId(credentials.deptId());
    loginUser.setRoles(credentials.roles());
    loginUser.setPermissions(credentials.permissions());

    // 清除失败次数
    clearLoginFailure(username);

    // 通过 AuthProvider 抽象接口登录（支持 Sa-Token / OAuth2 等多种实现）
    String token = authProvider.login(loginUser);

    // 记录登录日志
    if (securityConfig.isLoginLogEnabled()) {
      log.info("用户 {} 登录成功，IP: {}", username, ServletUtils.getClientIp());
    }

    return new LoginVO(token, "Authorization", loginUser);
  }

  /** 登出 */
  public void logout() {
    authProvider.logout();
  }

  /** 获取当前登录用户信息 */
  public UserInfoVO getUserInfo() {
    LoginUser loginUser = authProvider.getLoginUser();
    if (loginUser == null) {
      throw new ServiceException("请先登录");
    }

    return new UserInfoVO(loginUser, loginUser.getRoles(), loginUser.getPermissions());
  }

  /** 检查 IP 限流 */
  private void checkIpRateLimit() {
    if (!securityConfig.isIpRateLimitEnabled()) {
      return;
    }

    String ip = ServletUtils.getClientIp();
    String key = CacheConstants.LOGIN_ERROR_KEY + "ip:" + ip;

    Long count = redisUtils.increment(key);
    if (count == null) {
      return;
    }

    if (count == 1L) {
      redisUtils.expire(key, securityConfig.getIpRateLimitWindow(), TimeUnit.SECONDS);
    }

    if (count > securityConfig.getIpRateLimitMax()) {
      log.warn("IP {} 登录请求过于频繁，已被限流", ip);
      throw new ServiceException("请求过于频繁，请稍后重试");
    }
  }

  /** 检查账户是否被锁定 */
  private void checkAccountLocked(String username) {
    String lockKey = CacheConstants.PWD_ERR_CNT_KEY + "lock:" + username;
    if (redisUtils.hasKey(lockKey)) {
      Long ttl = redisUtils.getExpire(lockKey);
      if (ttl == null || ttl < 0) {
        throw new ServiceException("账户已被锁定，请稍后重试");
      }
      long minutes = Math.max(1, ttl / 60);
      throw new ServiceException("账户已被锁定，请 " + minutes + " 分钟后重试");
    }
  }

  /** 记录登录失败 */
  private void recordLoginFailure(String username) {
    String countKey = CacheConstants.PWD_ERR_CNT_KEY + username;

    Long failCount = redisUtils.increment(countKey);
    if (failCount == null) {
      failCount = 1L;
    }

    if (failCount == 1L) {
      redisUtils.expire(countKey, securityConfig.getFailCountResetDuration(), TimeUnit.MINUTES);
    }

    if (failCount >= securityConfig.getMaxFailAttempts()) {
      // 锁定账户
      String lockKey = CacheConstants.PWD_ERR_CNT_KEY + "lock:" + username;
      redisUtils.set(lockKey, "1", securityConfig.getLockDuration(), TimeUnit.MINUTES);
      redisUtils.delete(countKey);

      log.warn("用户 {} 登录失败次数过多，账户已被锁定 {} 分钟", username, securityConfig.getLockDuration());
      throw new ServiceException("登录失败次数过多，账户已被锁定 " + securityConfig.getLockDuration() + " 分钟");
    }

    int remaining = securityConfig.getMaxFailAttempts() - failCount.intValue();
    log.warn("用户 {} 登录失败，还剩 {} 次尝试机会", username, remaining);
  }

  /** 清除登录失败记录 */
  private void clearLoginFailure(String username) {
    String countKey = CacheConstants.PWD_ERR_CNT_KEY + username;
    redisUtils.delete(countKey);
  }
}
