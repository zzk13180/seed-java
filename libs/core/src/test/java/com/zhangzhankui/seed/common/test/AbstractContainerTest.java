package com.zhangzhankui.seed.common.test;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers 基础配置
 *
 * <p>提供 PostgreSQL 和 Redis 容器配置，子类可继承使用
 *
 * <p>注意：子类必须添加 @Testcontainers 注解才能启动容器
 */
@Testcontainers
@SuppressWarnings("resource")
public abstract class AbstractContainerTest {

  /** PostgreSQL 容器 */
  @Container
  protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
          .withDatabaseName("seed_test")
          .withUsername("test")
          .withPassword("test")
          .withReuse(true);

  /** Redis 容器 */
  @Container
  protected static final GenericContainer<?> REDIS_CONTAINER =
      new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
          .withExposedPorts(6379)
          .withReuse(true);

  /** 动态配置数据源属性 */
  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    // PostgreSQL 配置
    registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

    // Redis 配置
    registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
    registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));

    // 禁用 Nacos（测试环境）
    registry.add("spring.cloud.nacos.discovery.enabled", () -> false);
    registry.add("spring.cloud.nacos.config.enabled", () -> false);
  }

  /** 检查容器是否运行 */
  protected static boolean areContainersRunning() {
    return POSTGRES_CONTAINER.isRunning() && REDIS_CONTAINER.isRunning();
  }
}
