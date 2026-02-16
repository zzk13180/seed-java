package com.zhangzhankui.seed.system.integration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangzhankui.seed.system.SeedSystemApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * 集成测试 - 使用 Testcontainers 模拟真实环境
 *
 * <p>特点： 1. 使用真实的 PostgreSQL 和 Redis 容器 2. 测试完整的请求-响应流程 3. 验证数据库操作是否正确
 */
@SpringBootTest(
    classes = SeedSystemApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("系统模块集成测试")
@Tag("integration")
class SystemIntegrationTest {

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

  static boolean isDockerAvailable() {
    try {
      org.testcontainers.DockerClientFactory.instance().client();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @Order(1)
  @DisplayName("健康检查端点应该正常")
  void healthCheckShouldWork() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Nested
  @DisplayName("用户管理 API 集成测试")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class UserApiTests {

    @Test
    @Order(1)
    @DisplayName("查询用户列表 - 应返回预置用户")
    void shouldReturnUserList() throws Exception {
      mockMvc
          .perform(get("/user/list").param("pageNum", "1").param("pageSize", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code", is(200)))
          .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(2)
    @DisplayName("创建用户 - 完整流程测试")
    void shouldCreateUserEndToEnd() throws Exception {
      String requestBody =
          """
          {
              "username": "integration_user",
              "password": "Test@123456",
              "nickname": "集成测试用户",
              "email": "integration@example.com",
              "phone": "13800138001",
              "status": 1
          }
          """;

      mockMvc
          .perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code", is(200)));
    }

    @Test
    @Order(3)
    @DisplayName("查询用户列表 - 应包含新创建的用户")
    void shouldReturnCreatedUser() throws Exception {
      mockMvc
          .perform(get("/user/list").param("username", "integration_user"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.records[0].username", is("integration_user")));
    }

    @Test
    @Order(4)
    @DisplayName("通过内部接口获取用户信息")
    void shouldGetUserByInternalApi() throws Exception {
      mockMvc
          .perform(get("/user/info/admin").header("X-From-Source", "inner"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code", is(200)))
          .andExpect(jsonPath("$.data.username", is("admin")));
    }
  }

  @Nested
  @DisplayName("数据一致性测试")
  class DataConsistencyTests {

    @Test
    @DisplayName("用户名唯一性约束 - 重复用户名应失败")
    void shouldPreventDuplicateUsername() throws Exception {
      String requestBody =
          """
          {
              "username": "unique_test_user",
              "password": "Test@123456",
              "nickname": "唯一用户"
          }
          """;

      // 第一次创建应成功
      mockMvc
          .perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(requestBody))
          .andExpect(status().isOk());

      // 第二次创建相同用户名应失败（数据库约束异常会导致 500 错误）
      mockMvc
          .perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(requestBody))
          .andExpect(status().is5xxServerError());
    }
  }

  @Nested
  @DisplayName("边界条件测试")
  class EdgeCaseTests {

    @Test
    @DisplayName("查询不存在的用户应返回空数据")
    void shouldReturnEmptyForNonexistentUser() throws Exception {
      mockMvc
          .perform(get("/user/info/definitely_not_exists_user").header("X-From-Source", "inner"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("分页参数边界测试")
    void shouldHandlePaginationEdgeCases() throws Exception {
      // 页码为0
      mockMvc
          .perform(get("/user/list").param("pageNum", "0").param("pageSize", "10"))
          .andExpect(status().isOk());

      // 极大页码
      mockMvc
          .perform(get("/user/list").param("pageNum", "9999").param("pageSize", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.records").isEmpty());
    }
  }
}
