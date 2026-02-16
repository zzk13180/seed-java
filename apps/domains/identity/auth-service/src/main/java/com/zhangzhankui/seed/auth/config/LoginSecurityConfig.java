package com.zhangzhankui.seed.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 登录安全配置
 *
 * <p>现代安全策略： 1. 登录失败次数限制 - 防止暴力破解 2. 登录失败锁定时间 - 账户保护 3. IP级别限流 - 防止分布式攻击 4. 可选的 MFA 支持
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "seed.security.login")
public class LoginSecurityConfig {

  /** 最大登录失败次数，超过后锁定账户 */
  private int maxFailAttempts = 5;

  /** 账户锁定时间（分钟） */
  private int lockDuration = 30;

  /** 登录失败次数重置时间（分钟） */
  private int failCountResetDuration = 10;

  /** 是否启用 IP 级别限流 */
  private boolean ipRateLimitEnabled = true;

  /** IP 限流时间窗口（秒） */
  private int ipRateLimitWindow = 60;

  /** IP 限流窗口内最大请求次数 */
  private int ipRateLimitMax = 10;

  /** 是否启用设备指纹验证 */
  private boolean deviceFingerprintEnabled;

  /** 是否记录登录日志 */
  private boolean loginLogEnabled = true;
}
