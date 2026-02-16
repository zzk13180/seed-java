package com.zhangzhankui.seed.common.security.inner;

import com.zhangzhankui.seed.common.core.constant.SecurityConstants;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 内部认证 HMAC 签名工具
 *
 * <p>使用共享密钥对内部服务间请求进行 HMAC-SHA256 签名， 防止伪造 {@code X-From-Source: inner} 请求头绕过认证。
 *
 * <p>签名算法：{@code HMAC-SHA256(secret, "inner:" + timestamp)}
 *
 * <p>防重放：签名附带时间戳，验证端拒绝超过 {@link SecurityConstants#INNER_AUTH_SIGN_TTL_MS} 的请求。
 */
public class InnerAuthSigner {

  private static final Logger log = LoggerFactory.getLogger(InnerAuthSigner.class);
  private static final String ALGORITHM = "HmacSHA256";

  private final byte[] secretBytes;

  public InnerAuthSigner(String secret) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalArgumentException("内部认证密钥不能为空，请配置 seed.security.inner-auth-secret");
    }
    this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
  }

  /** 生成 HMAC 签名 */
  public String sign(long timestamp) {
    String data = SecurityConstants.INNER + ":" + timestamp;
    return hmacSha256(data);
  }

  /**
   * 验证 HMAC 签名
   *
   * @param signature 客户端传递的签名
   * @param timestamp 客户端传递的时间戳
   * @return 签名是否有效（含时效性检查）
   */
  public boolean verify(String signature, String timestamp) {
    if (signature == null || timestamp == null) {
      return false;
    }

    long ts;
    try {
      ts = Long.parseLong(timestamp);
    } catch (NumberFormatException e) {
      log.warn("内部认证时间戳格式无效: {}", timestamp);
      return false;
    }

    // 防重放：检查时间戳是否在有效范围内
    long now = System.currentTimeMillis();
    if (Math.abs(now - ts) > SecurityConstants.INNER_AUTH_SIGN_TTL_MS) {
      log.warn("内部认证签名已过期, timestamp={}, now={}, diff={}ms", ts, now, now - ts);
      return false;
    }

    // 验证签名
    String expected = sign(ts);
    return constantTimeEquals(expected, signature);
  }

  private String hmacSha256(String data) {
    try {
      Mac mac = Mac.getInstance(ALGORITHM);
      mac.init(new SecretKeySpec(secretBytes, ALGORITHM));
      byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (Exception e) {
      throw new IllegalStateException("HMAC-SHA256 签名失败", e);
    }
  }

  /** 常量时间比较，防止时序攻击 */
  private static boolean constantTimeEquals(String a, String b) {
    if (a.length() != b.length()) {
      return false;
    }
    int result = 0;
    for (int i = 0; i < a.length(); i++) {
      result |= a.charAt(i) ^ b.charAt(i);
    }
    return result == 0;
  }
}
