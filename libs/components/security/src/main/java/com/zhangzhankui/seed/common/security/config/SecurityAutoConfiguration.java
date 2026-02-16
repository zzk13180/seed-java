package com.zhangzhankui.seed.common.security.config;

import com.zhangzhankui.seed.common.core.auth.AuthProvider;
import com.zhangzhankui.seed.common.core.context.UserContextHolder;
import com.zhangzhankui.seed.common.security.aspect.InnerAuthAspect;
import com.zhangzhankui.seed.common.security.handler.GlobalExceptionHandler;
import com.zhangzhankui.seed.common.security.inner.InnerAuthSigner;
import com.zhangzhankui.seed.common.security.provider.AuthProperties;
import com.zhangzhankui.seed.common.security.provider.AuthProviderUserContextHolder;
import com.zhangzhankui.seed.common.security.provider.OAuth2AuthProvider;
import com.zhangzhankui.seed.common.security.provider.SaTokenAuthProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全模块自动配置
 *
 * <p>根据配置自动选择认证方案：
 *
 * <pre>
 * seed:
 *   auth:
 *     provider: satoken  # 或 oauth2
 * </pre>
 *
 * <p>默认使用 Sa-Token，配置 provider=oauth2 切换到 OAuth2/OIDC
 */
@AutoConfiguration
@EnableConfigurationProperties(AuthProperties.class)
@Import({GlobalExceptionHandler.class})
public class SecurityAutoConfiguration {

  /**
   * 内部认证 HMAC 签名器
   *
   * <p>使用共享密钥对内部 RPC 请求签名，防止伪造 X-From-Source header
   */
  @Bean
  @ConditionalOnMissingBean
  public InnerAuthSigner innerAuthSigner(
      @Value("${seed.security.inner-auth-secret:}") String secret) {
    if (secret.isBlank()) {
      throw new IllegalStateException(
          "必须配置 seed.security.inner-auth-secret（内部服务间 HMAC 签名密钥），"
              + "可通过环境变量 INNER_AUTH_SECRET 注入");
    }
    return new InnerAuthSigner(secret);
  }

  /** 内部认证切面 */
  @Bean
  @ConditionalOnMissingBean
  public InnerAuthAspect innerAuthAspect(InnerAuthSigner innerAuthSigner) {
    return new InnerAuthAspect(innerAuthSigner);
  }

  /**
   * Sa-Token 认证提供者（默认）
   *
   * <p>当 seed.auth.provider=satoken 或未配置时激活
   */
  @Bean
  @Primary
  @ConditionalOnProperty(
      name = "seed.auth.provider",
      havingValue = "satoken",
      matchIfMissing = true)
  public AuthProvider saTokenAuthProvider() {
    return new SaTokenAuthProvider();
  }

  /**
   * OAuth2/OIDC 认证提供者
   *
   * <p>当 seed.auth.provider=oauth2 时激活
   */
  @Bean
  @ConditionalOnProperty(name = "seed.auth.provider", havingValue = "oauth2")
  public AuthProvider oAuth2AuthProvider() {
    return new OAuth2AuthProvider();
  }

  /**
   * 统一的用户上下文实现
   *
   * <p>委托给当前激活的 AuthProvider
   */
  @Bean
  @Primary
  public UserContextHolder authProviderUserContextHolder(AuthProvider authProvider) {
    return new AuthProviderUserContextHolder(authProvider);
  }

  /**
   * 密码编码器 - 使用 BCrypt 算法
   *
   * <p>Sa-Token 模式下用于本地密码验证
   */
  @Bean
  @ConditionalOnMissingBean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
