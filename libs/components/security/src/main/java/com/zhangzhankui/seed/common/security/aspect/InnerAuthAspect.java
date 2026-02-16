package com.zhangzhankui.seed.common.security.aspect;

import com.zhangzhankui.seed.common.core.annotation.InnerAuth;
import com.zhangzhankui.seed.common.core.constant.SecurityConstants;
import com.zhangzhankui.seed.common.core.exception.InnerAuthException;
import com.zhangzhankui.seed.common.core.utils.ServletUtils;
import com.zhangzhankui.seed.common.security.inner.InnerAuthSigner;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 内部认证切面
 *
 * <p>安全机制：
 *
 * <ol>
 *   <li>网关 AuthFilter 清除外部请求中的 X-From-Source header
 *   <li>HMAC-SHA256 签名验证请求来自可信内部服务（共享密钥）
 *   <li>时间戳防重放攻击（5 分钟有效期）
 * </ol>
 *
 * <p>即使微服务端口意外暴露，攻击者也无法伪造有效签名。
 */
@Aspect
@Component
public class InnerAuthAspect implements Ordered {

  private final InnerAuthSigner innerAuthSigner;

  public InnerAuthAspect(InnerAuthSigner innerAuthSigner) {
    this.innerAuthSigner = innerAuthSigner;
  }

  @Around("@annotation(innerAuth)")
  public Object around(ProceedingJoinPoint point, InnerAuth innerAuth) throws Throwable {
    String source = ServletUtils.getHeader(SecurityConstants.HEADER_FROM_SOURCE);

    // 非内部请求拒绝
    if (!SecurityConstants.INNER.equals(source)) {
      throw new InnerAuthException("没有内部访问权限，不允许访问");
    }

    // HMAC 签名验证
    String signature = ServletUtils.getHeader(SecurityConstants.HEADER_INNER_AUTH_SIGN);
    String timestamp = ServletUtils.getHeader(SecurityConstants.HEADER_INNER_AUTH_TIMESTAMP);
    if (!innerAuthSigner.verify(signature, timestamp)) {
      throw new InnerAuthException("内部认证签名验证失败");
    }

    // 需要验证用户信息
    if (innerAuth.isUser()) {
      String userId = ServletUtils.getHeader(SecurityConstants.HEADER_USER_ID);
      if (userId == null || userId.isEmpty()) {
        throw new InnerAuthException("没有用户信息，不允许访问");
      }
    }

    return point.proceed();
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE + 1;
  }
}
