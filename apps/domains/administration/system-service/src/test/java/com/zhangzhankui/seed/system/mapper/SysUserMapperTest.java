package com.zhangzhankui.seed.system.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import com.zhangzhankui.seed.system.SeedSystemApplication;
import com.zhangzhankui.seed.system.domain.SysUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * SysUserMapper 测试
 *
 * <p>使用 Testcontainers 启动真实 PostgreSQL 进行测试
 */
@SpringBootTest(classes = SeedSystemApplication.class)
@Testcontainers
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("SysUserMapper 集成测试")
@Transactional
class SysUserMapperTest {

  @Container
  @SuppressWarnings("resource")
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
          .withDatabaseName("seed_test")
          .withUsername("test")
          .withPassword("test")
          .withInitScript("db/init-test.sql");

  @Container
  @SuppressWarnings("resource")
  static GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    // PostgreSQL - 使用 dynamic-datasource 属性
    registry.add("spring.datasource.dynamic.datasource.master.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.dynamic.datasource.master.username", postgres::getUsername);
    registry.add("spring.datasource.dynamic.datasource.master.password", postgres::getPassword);

    // Redis
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

    // 禁用 Nacos
    registry.add("spring.cloud.nacos.discovery.enabled", () -> false);
    registry.add("spring.cloud.nacos.config.enabled", () -> false);
  }

  @Autowired private SysUserMapper userMapper;

  @Nested
  @DisplayName("selectByUsername - 根据用户名查询")
  class SelectByUsernameTests {

    @Test
    @DisplayName("应返回存在的用户")
    void shouldReturnUserWhenExists() {
      // Act
      SysUser user = userMapper.selectByUsername("admin");

      // Assert
      assertThat(user).isNotNull();
      assertThat(user.getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("用户不存在时应返回 null")
    void shouldReturnNullWhenNotExists() {
      // Act
      SysUser user = userMapper.selectByUsername("nonexistent");

      // Assert
      assertThat(user).isNull();
    }
  }

  @Nested
  @DisplayName("selectRoleKeysByUserId - 查询用户角色")
  class SelectRoleKeysTests {

    @Test
    @DisplayName("应返回用户的角色标识")
    void shouldReturnRoleKeys() {
      // Act
      Set<String> roles = userMapper.selectRoleKeysByUserId(1L);

      // Assert
      assertThat(roles).isNotEmpty().contains("admin");
    }
  }

  @Nested
  @DisplayName("selectPermsByUserId - 查询用户权限")
  class SelectPermsTests {

    @Test
    @DisplayName("应返回用户的权限标识")
    void shouldReturnPermissions() {
      // Act
      Set<String> perms = userMapper.selectPermsByUserId(1L);

      // Assert
      assertThat(perms).isNotNull();
    }
  }

  @Nested
  @DisplayName("CRUD 操作测试")
  class CrudTests {

    @Test
    @DisplayName("应成功插入新用户")
    void shouldInsertUser() {
      // Arrange
      SysUser newUser = new SysUser();
      newUser.setUsername("testuser");
      newUser.setPassword("encrypted_password");
      newUser.setNickname("测试用户");
      newUser.setEmail("test@example.com");
      newUser.setStatus(1);

      // Act
      int result = userMapper.insert(newUser);

      // Assert
      assertThat(result).isEqualTo(1);
      assertThat(newUser.getUserId()).isNotNull();
    }

    @Test
    @DisplayName("应成功更新用户")
    void shouldUpdateUser() {
      // Arrange
      SysUser user = userMapper.selectByUsername("admin");
      user.setNickname("更新后的管理员");

      // Act
      int result = userMapper.updateById(user);

      // Assert
      assertThat(result).isEqualTo(1);

      SysUser updated = userMapper.selectById(user.getUserId());
      assertThat(updated.getNickname()).isEqualTo("更新后的管理员");
    }
  }
}
