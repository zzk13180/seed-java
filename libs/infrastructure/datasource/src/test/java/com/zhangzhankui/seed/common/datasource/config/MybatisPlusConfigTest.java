package com.zhangzhankui.seed.common.datasource.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.zhangzhankui.seed.common.core.context.UserContextHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** MybatisPlus配置测试 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MybatisPlus配置测试")
class MybatisPlusConfigTest {

  @Mock private UserContextHolder userContextHolder;

  @Test
  @DisplayName("配置类应正确实例化")
  void shouldCreateConfigInstance() {
    MybatisPlusConfig config = new MybatisPlusConfig(userContextHolder);
    assertThat(config).isNotNull();
  }

  @Test
  @DisplayName("应实现MetaObjectHandler接口")
  void shouldImplementMetaObjectHandler() {
    MybatisPlusConfig config = new MybatisPlusConfig(userContextHolder);
    assertThat(config).isInstanceOf(com.baomidou.mybatisplus.core.handlers.MetaObjectHandler.class);
  }

  @Test
  @DisplayName("应有AutoConfiguration注解")
  void shouldHaveAutoConfigurationAnnotation() {
    Class<MybatisPlusConfig> clazz = MybatisPlusConfig.class;
    assertThat(
            clazz.isAnnotationPresent(
                org.springframework.boot.autoconfigure.AutoConfiguration.class))
        .isTrue();
  }
}
