package com.zhangzhankui.seed.common.log.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 异步日志配置测试 */
@DisplayName("异步日志配置测试")
class AsyncLogConfigTest {

  @Test
  @DisplayName("应能创建配置实例")
  void shouldCreateConfigInstance() {
    AsyncLogConfig config = new AsyncLogConfig();
    assertThat(config).isNotNull();
  }

  @Test
  @DisplayName("应有Configuration注解")
  void shouldHaveConfigurationAnnotation() {
    Class<AsyncLogConfig> clazz = AsyncLogConfig.class;
    assertThat(
            clazz.isAnnotationPresent(org.springframework.context.annotation.Configuration.class))
        .isTrue();
  }

  @Test
  @DisplayName("应有EnableAsync注解")
  void shouldHaveEnableAsyncAnnotation() {
    Class<AsyncLogConfig> clazz = AsyncLogConfig.class;
    assertThat(
            clazz.isAnnotationPresent(org.springframework.scheduling.annotation.EnableAsync.class))
        .isTrue();
  }
}
