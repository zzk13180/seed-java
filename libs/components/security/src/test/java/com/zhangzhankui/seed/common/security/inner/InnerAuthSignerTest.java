package com.zhangzhankui.seed.common.security.inner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("InnerAuthSigner 单元测试")
class InnerAuthSignerTest {

  private static final String TEST_SECRET = "test-hmac-secret-key-for-unit-tests";

  @Nested
  @DisplayName("构造函数")
  class ConstructorTests {

    @Test
    @DisplayName("空密钥应抛出异常")
    void shouldThrowExceptionWhenSecretIsEmpty() {
      assertThatThrownBy(() -> new InnerAuthSigner(""))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("内部认证密钥不能为空");
    }

    @Test
    @DisplayName("null 密钥应抛出异常")
    void shouldThrowExceptionWhenSecretIsNull() {
      assertThatThrownBy(() -> new InnerAuthSigner(null))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("签名和验证")
  class SignAndVerifyTests {

    private final InnerAuthSigner signer = new InnerAuthSigner(TEST_SECRET);

    @Test
    @DisplayName("相同时间戳应生成相同签名")
    void shouldGenerateSameSignatureForSameTimestamp() {
      long timestamp = System.currentTimeMillis();
      String sign1 = signer.sign(timestamp);
      String sign2 = signer.sign(timestamp);
      assertThat(sign1).isEqualTo(sign2);
    }

    @Test
    @DisplayName("不同时间戳应生成不同签名")
    void shouldGenerateDifferentSignatureForDifferentTimestamp() {
      String sign1 = signer.sign(1000L);
      String sign2 = signer.sign(2000L);
      assertThat(sign1).isNotEqualTo(sign2);
    }

    @Test
    @DisplayName("有效签名应验证通过")
    void shouldVerifyValidSignature() {
      long timestamp = System.currentTimeMillis();
      String signature = signer.sign(timestamp);
      assertThat(signer.verify(signature, String.valueOf(timestamp))).isTrue();
    }

    @Test
    @DisplayName("伪造签名应验证失败")
    void shouldRejectForgedSignature() {
      long timestamp = System.currentTimeMillis();
      assertThat(signer.verify("forged-signature", String.valueOf(timestamp))).isFalse();
    }

    @Test
    @DisplayName("过期时间戳应验证失败")
    void shouldRejectExpiredTimestamp() {
      long oldTimestamp = System.currentTimeMillis() - 10 * 60 * 1000L; // 10 分钟前
      String signature = signer.sign(oldTimestamp);
      assertThat(signer.verify(signature, String.valueOf(oldTimestamp))).isFalse();
    }

    @Test
    @DisplayName("null 签名应验证失败")
    void shouldRejectNullSignature() {
      assertThat(signer.verify(null, String.valueOf(System.currentTimeMillis()))).isFalse();
    }

    @Test
    @DisplayName("null 时间戳应验证失败")
    void shouldRejectNullTimestamp() {
      assertThat(signer.verify("some-signature", null)).isFalse();
    }

    @Test
    @DisplayName("无效时间戳格式应验证失败")
    void shouldRejectInvalidTimestampFormat() {
      assertThat(signer.verify("some-signature", "not-a-number")).isFalse();
    }

    @Test
    @DisplayName("不同密钥生成的签名应验证失败")
    void shouldRejectSignatureFromDifferentSecret() {
      InnerAuthSigner otherSigner = new InnerAuthSigner("different-secret-key");
      long timestamp = System.currentTimeMillis();
      String signature = otherSigner.sign(timestamp);
      assertThat(signer.verify(signature, String.valueOf(timestamp))).isFalse();
    }
  }
}
