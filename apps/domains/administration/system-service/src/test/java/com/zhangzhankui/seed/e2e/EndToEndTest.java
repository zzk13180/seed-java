package com.zhangzhankui.seed.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.time.Duration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * E2E (端到端) 测试
 *
 * <p>使用 Docker Compose 启动完整的微服务环境进行测试，模拟真实用户的完整业务流程。
 *
 * <p>本测试使用 Testcontainers 自动管理 Docker Compose 环境：
 * <ul>
 *   <li>docker-compose.yml - 基础服务定义</li>
 *   <li>docker-compose.test.yml - 测试环境覆盖配置 (独立端口和 volume)</li>
 * </ul>
 *
 * <p>API 路径规范： - 认证服务: /auth/* - 系统服务: /system/*
 *
 * <p>注意：本测试使用不带 /api 前缀的路径，与网关路由配置保持一致
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("端到端测试 - 完整业务流程")
@Tag("e2e")
class EndToEndTest {

  private static String authToken;

  private static final String DOCKER_COMPOSE_PATH = "../../../../ops/deployment/docker/docker-compose.yml";
  private static final String DOCKER_COMPOSE_TEST_PATH = "../../../../ops/deployment/docker/docker-compose.test.yml";

  @Container
  @SuppressWarnings("resource")
  static DockerComposeContainer<?> environment =
      new DockerComposeContainer<>(
              new File(DOCKER_COMPOSE_PATH),
              new File(DOCKER_COMPOSE_TEST_PATH))
          .withLocalCompose(true)  // 使用本地 docker compose 命令而不是容器化版本
          .withOptions("--profile=test")
          .withExposedService(
              "seed-test-gateway",
              8080,
              Wait.forHttp("/actuator/health")
                  .forStatusCode(200)
                  .withStartupTimeout(Duration.ofMinutes(3)))
          .withExposedService(
              "seed-test-postgres", 5432, Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)))
          .withExposedService(
              "seed-test-redis", 6379, Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(1)));

  static boolean isDockerAvailable() {
    try {
      org.testcontainers.DockerClientFactory.instance().client();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @BeforeAll
  static void setUp() {
    String gatewayHost = environment.getServiceHost("seed-test-gateway", 8080);
    Integer gatewayPort = environment.getServicePort("seed-test-gateway", 8080);
    RestAssured.baseURI = "http://" + gatewayHost;
    RestAssured.port = gatewayPort;
  }

  @Nested
  @DisplayName("用户认证流程")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class AuthFlowTests {

    @Test
    @Order(1)
    @DisplayName("1. 用户登录 - 获取Token")
    void shouldLoginAndGetToken() {
      Response response =
          given()
              .contentType(ContentType.JSON)
              .body(
                  """
                  {
                      "username": "admin",
                      "password": "admin123"
                  }
                  """)
              .when()
              .post("/auth/login")
              .then()
              .statusCode(200)
              .body("code", equalTo(200))
              .body("data.token", notNullValue())
              .body("data.expiresIn", greaterThan(0))
              .extract()
              .response();

      authToken = response.jsonPath().getString("data.token");
      System.out.println("获取到Token: " + authToken.substring(0, 20) + "...");
    }

    @Test
    @Order(2)
    @DisplayName("2. 使用Token访问受保护资源")
    void shouldAccessProtectedResource() {
      given()
          .header("Authorization", "Bearer " + authToken)
          .when()
          .get("/system/users/info")
          .then()
          .statusCode(200)
          .body("data.username", equalTo("admin"));
    }

    @Test
    @Order(3)
    @DisplayName("3. Token刷新")
    void shouldRefreshToken() {
      given()
          .header("Authorization", "Bearer " + authToken)
          .when()
          .post("/auth/refresh")
          .then()
          .statusCode(200)
          .body("data.token", notNullValue());
    }

    @Test
    @Order(4)
    @DisplayName("4. 用户登出")
    void shouldLogout() {
      given()
          .header("Authorization", "Bearer " + authToken)
          .when()
          .post("/auth/logout")
          .then()
          .statusCode(200);
    }
  }

  @Nested
  @DisplayName("用户管理完整流程")
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class UserManagementFlowTests {

    private static Long createdUserId;

    @BeforeAll
    static void login() {
      // 获取管理员Token
      authToken =
          given()
              .contentType(ContentType.JSON)
              .body("{\"username\":\"admin\",\"password\":\"admin123\"}")
              .post("/auth/login")
              .jsonPath()
              .getString("data.token");
    }

    @Test
    @Order(1)
    @DisplayName("1. 创建新用户")
    void shouldCreateUser() {
      Response response =
          given()
              .header("Authorization", "Bearer " + authToken)
              .contentType(ContentType.JSON)
              .body(
                  """
                  {
                      "username": "e2e_test_user",
                      "password": "Test@123456",
                      "nickname": "E2E测试用户",
                      "email": "e2e@example.com",
                      "phone": "13900139000",
                      "deptId": 2,
                      "roleIds": [2]
                  }
                  """)
              .when()
              .post("/system/users")
              .then()
              .statusCode(201)
              .body("code", equalTo(200))
              .extract()
              .response();

      createdUserId = response.jsonPath().getLong("data.id");
    }

    @Test
    @Order(2)
    @DisplayName("2. 查询用户列表 - 应包含新用户")
    void shouldFindCreatedUser() {
      given()
          .header("Authorization", "Bearer " + authToken)
          .queryParam("username", "e2e_test_user")
          .when()
          .get("/system/users")
          .then()
          .statusCode(200)
          .body("data.records", hasSize(greaterThanOrEqualTo(1)))
          .body("data.records[0].username", equalTo("e2e_test_user"));
    }

    @Test
    @Order(3)
    @DisplayName("3. 新用户登录验证")
    void newUserShouldLogin() {
      given()
          .contentType(ContentType.JSON)
          .body(
              """
              {
                  "username": "e2e_test_user",
                  "password": "Test@123456"
              }
              """)
          .when()
          .post("/auth/login")
          .then()
          .statusCode(200)
          .body("data.token", notNullValue());
    }

    @Test
    @Order(4)
    @DisplayName("4. 修改用户信息")
    void shouldUpdateUser() {
      given()
          .header("Authorization", "Bearer " + authToken)
          .contentType(ContentType.JSON)
          .body(
              """
              {
                  "nickname": "E2E测试用户-已修改",
                  "email": "e2e_updated@example.com"
              }
              """)
          .when()
          .put("/system/users/" + createdUserId)
          .then()
          .statusCode(200);
    }

    @Test
    @Order(5)
    @DisplayName("5. 重置用户密码")
    void shouldResetPassword() {
      given()
          .header("Authorization", "Bearer " + authToken)
          .contentType(ContentType.JSON)
          .body("{\"newPassword\": \"NewPass@123\"}")
          .when()
          .put("/system/users/" + createdUserId + "/reset-password")
          .then()
          .statusCode(200);
    }

    @Test
    @Order(6)
    @DisplayName("6. 禁用用户")
    void shouldDisableUser() {
      given()
          .header("Authorization", "Bearer " + authToken)
          .when()
          .put("/system/users/" + createdUserId + "/disable")
          .then()
          .statusCode(200);
    }

    @Test
    @Order(7)
    @DisplayName("7. 禁用后用户无法登录")
    void disabledUserShouldNotLogin() {
      given()
          .contentType(ContentType.JSON)
          .body(
              """
              {
                  "username": "e2e_test_user",
                  "password": "NewPass@123"
              }
              """)
          .when()
          .post("/auth/login")
          .then()
          .statusCode(401)
          .body("message", containsString("已禁用"));
    }

    @Test
    @Order(8)
    @DisplayName("8. 删除用户 - 清理测试数据")
    void shouldDeleteUser() {
      given()
          .header("Authorization", "Bearer " + authToken)
          .when()
          .delete("/system/users/" + createdUserId)
          .then()
          .statusCode(200);
    }
  }

  @Nested
  @DisplayName("权限控制测试")
  class PermissionTests {

    @Test
    @DisplayName("普通用户不能访问管理接口")
    void normalUserShouldNotAccessAdminApi() {
      // 使用普通用户登录
      String userToken =
          given()
              .contentType(ContentType.JSON)
              .body("{\"username\":\"user\",\"password\":\"user123\"}")
              .post("/auth/login")
              .jsonPath()
              .getString("data.token");

      // 尝试访问管理接口
      given()
          .header("Authorization", "Bearer " + userToken)
          .when()
          .get("/system/users")
          .then()
          .statusCode(403)
          .body("message", containsString("权限不足"));
    }
  }

  @Nested
  @DisplayName("限流与防护测试")
  class RateLimitTests {

    @Test
    @DisplayName("登录接口应有限流保护")
    void loginShouldBeRateLimited() {
      // 快速发送多次请求
      for (int i = 0; i < 10; i++) {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\":\"admin\",\"password\":\"wrongpassword\"}")
            .post("/auth/login");
      }

      // 第11次应该被限流
      given()
          .contentType(ContentType.JSON)
          .body("{\"username\":\"admin\",\"password\":\"wrongpassword\"}")
          .when()
          .post("/auth/login")
          .then()
          .statusCode(429)
          .body("message", containsString("请求过于频繁"));
    }
  }
}
