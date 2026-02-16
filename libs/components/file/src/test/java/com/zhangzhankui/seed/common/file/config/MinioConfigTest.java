package com.zhangzhankui.seed.common.file.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Minio配置测试 */
@DisplayName("Minio配置测试")
class MinioConfigTest {

  @Test
  @DisplayName("配置类应有正确的注解")
  void shouldHaveConfigurationPropertiesAnnotation() {
    Class<MinioConfig> clazz = MinioConfig.class;

    // 检查是否有@ConfigurationProperties注解
    ConfigurationProperties annotation = clazz.getAnnotation(ConfigurationProperties.class);
    assertThat(annotation).isNotNull();
    assertThat(annotation.prefix()).isEqualTo("minio");
  }

  @Test
  @DisplayName("应能创建配置实例")
  void shouldCreateConfigInstance() {
    MinioConfig config = new MinioConfig();
    assertThat(config).isNotNull();
  }
}
