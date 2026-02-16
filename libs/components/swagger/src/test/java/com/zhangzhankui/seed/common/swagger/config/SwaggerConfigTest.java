package com.zhangzhankui.seed.common.swagger.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Swagger配置测试 */
@DisplayName("Swagger配置测试")
class SwaggerConfigTest {

  @Test
  @DisplayName("应能创建配置实例")
  void shouldCreateConfigInstance() {
    SwaggerConfig config = new SwaggerConfig();
    assertThat(config).isNotNull();
  }

  @Test
  @DisplayName("应有AutoConfiguration注解")
  void shouldHaveConfigurationAnnotation() {
    Class<SwaggerConfig> clazz = SwaggerConfig.class;
    assertThat(
            clazz.isAnnotationPresent(
                org.springframework.boot.autoconfigure.AutoConfiguration.class))
        .isTrue();
  }

  @Test
  @DisplayName("应有OpenAPI相关配置")
  void shouldHaveOpenAPIConfiguration() {
    SwaggerConfig config = new SwaggerConfig();
    // 验证能创建OpenAPI bean
    assertThat(config.openAPI()).isNotNull();
  }
}
