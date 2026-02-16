package com.zhangzhankui.seed.system.e2e;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * 核心业务流程 E2E 测试
 *
 * <p>测试完整的业务场景，模拟真实用户操作。
 *
 * <p>启动测试环境:
 * <pre>
 * # 使用 Devtron 或本地 Docker Compose
 * cd ops/deployment/docker
 * docker compose -f docker-compose.yml -f docker-compose.test.yml --profile test up -d
 * </pre>
 *
 * <p>API 路径规范： - 认证服务: /auth/* - 系统服务: /system/*
 *
 * <p>注意：确保 API 路径与网关路由配置一致
 */
@Tag("e2e")
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("系统核心业务流程 E2E 测试")
class CoreBusinessE2ETest {

  // 默认使用测试环境端口 18080，可通过环境变量覆盖
  private static final String GATEWAY_URL =
      System.getenv().getOrDefault("GATEWAY_URL", "http://localhost:18080");
  private static String accessToken;
  private static Long createdUserId;

  @BeforeAll
  static void setUp() {
    RestAssured.baseURI = GATEWAY_URL;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    System.out.println("Starting E2E tests with Gateway URL: " + GATEWAY_URL);

    // 等待服务就绪
    await()
        .atMost(2, TimeUnit.MINUTES)
        .pollInterval(5, TimeUnit.SECONDS)
        .until(
            () -> {
              try {
                int status = given().when().get("/actuator/health").statusCode();
                System.out.println("Health check status: " + status);
                return status == 200;
              } catch (Exception e) {
                System.out.println("Health check failed: " + e.getMessage());
                return false;
              }
            });
  }

  @AfterAll
  static void tearDown() {
    // 清理静态变量
    accessToken = null;
    createdUserId = null;
    System.out.println("E2E tests completed.");
  }

  @Test
  @Order(1)
  @DisplayName("场景1: 管理员登录")
  void testAdminLogin() {
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("username", "admin");
    loginRequest.put("password", "admin123");

    Response response =
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
            .when()
            .post("/auth/login")
            .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.access_token", notNullValue())
            .extract()
            .response();

    accessToken = response.path("data.access_token");
    assertThat(accessToken).isNotBlank();
  }

  @Test
  @Order(2)
  @DisplayName("场景2: 获取当前用户信息")
  void testGetCurrentUserInfo() {
    given()
        .header("Authorization", "Bearer " + accessToken)
        .when()
        .get("/system/user/getInfo")
        .then()
        .statusCode(200)
        .body("code", equalTo(200))
        .body("data.user.username", equalTo("admin"))
        .body("data.roles", hasItem("admin"))
        .body("data.permissions", not(empty()));
  }

  @Test
  @Order(3)
  @DisplayName("场景3: 创建新用户")
  void testCreateUser() {
    Map<String, Object> newUser = new HashMap<>();
    newUser.put("username", "e2e_test_user_" + System.currentTimeMillis());
    newUser.put("nickName", "E2E测试用户");
    newUser.put("password", "Test@123456");
    newUser.put("email", "e2e@test.com");
    newUser.put("phone", "13800138000");
    newUser.put("sex", 1);
    newUser.put("status", 1);
    newUser.put("deptId", 1);
    newUser.put("roleIds", new Long[] {2L});

    Response response =
        given()
            .header("Authorization", "Bearer " + accessToken)
            .contentType(ContentType.JSON)
            .body(newUser)
            .when()
            .post("/system/user")
            .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .extract()
            .response();

    // 保存创建的用户ID，用于后续测试
    createdUserId = response.path("data.userId");
  }

  @Test
  @Order(4)
  @DisplayName("场景4: 查询用户列表")
  void testQueryUserList() {
    given()
        .header("Authorization", "Bearer " + accessToken)
        .queryParam("pageNum", 1)
        .queryParam("pageSize", 10)
        .when()
        .get("/system/user/list")
        .then()
        .statusCode(200)
        .body("code", equalTo(200))
        .body("data.rows", not(empty()))
        .body("data.total", greaterThan(0));
  }

  @Test
  @Order(5)
  @DisplayName("场景5: 根据ID获取用户详情")
  void testGetUserById() {
    Assumptions.assumeTrue(createdUserId != null, "需要先创建用户");

    given()
        .header("Authorization", "Bearer " + accessToken)
        .when()
        .get("/system/user/" + createdUserId)
        .then()
        .statusCode(200)
        .body("code", equalTo(200))
        .body("data.userId", equalTo(createdUserId.intValue()));
  }

  @Test
  @Order(6)
  @DisplayName("场景6: 更新用户信息")
  void testUpdateUser() {
    Assumptions.assumeTrue(createdUserId != null, "需要先创建用户");

    Map<String, Object> updateData = new HashMap<>();
    updateData.put("userId", createdUserId);
    updateData.put("nickName", "更新后的昵称");
    updateData.put("remark", "E2E测试更新");

    given()
        .header("Authorization", "Bearer " + accessToken)
        .contentType(ContentType.JSON)
        .body(updateData)
        .when()
        .put("/system/user")
        .then()
        .statusCode(200)
        .body("code", equalTo(200));

    // 验证更新成功
    given()
        .header("Authorization", "Bearer " + accessToken)
        .when()
        .get("/system/user/" + createdUserId)
        .then()
        .body("data.nickName", equalTo("更新后的昵称"));
  }

  @Test
  @Order(7)
  @DisplayName("场景7: 重置用户密码")
  void testResetPassword() {
    Assumptions.assumeTrue(createdUserId != null, "需要先创建用户");

    Map<String, Object> resetData = new HashMap<>();
    resetData.put("userId", createdUserId);
    resetData.put("password", "NewPassword@456");

    given()
        .header("Authorization", "Bearer " + accessToken)
        .contentType(ContentType.JSON)
        .body(resetData)
        .when()
        .put("/system/user/resetPwd")
        .then()
        .statusCode(200)
        .body("code", equalTo(200));
  }

  @Test
  @Order(8)
  @DisplayName("场景8: 删除用户")
  void testDeleteUser() {
    Assumptions.assumeTrue(createdUserId != null, "需要先创建用户");

    given()
        .header("Authorization", "Bearer " + accessToken)
        .when()
        .delete("/system/user/" + createdUserId)
        .then()
        .statusCode(200)
        .body("code", equalTo(200));

    // 验证已删除
    given()
        .header("Authorization", "Bearer " + accessToken)
        .when()
        .get("/system/user/" + createdUserId)
        .then()
        .body("data", nullValue());
  }

  @Test
  @Order(9)
  @DisplayName("场景9: 获取菜单树")
  void testGetMenuTree() {
    given()
        .header("Authorization", "Bearer " + accessToken)
        .when()
        .get("/system/menu/treeselect")
        .then()
        .statusCode(200)
        .body("code", equalTo(200))
        .body("data", not(empty()));
  }

  @Test
  @Order(10)
  @DisplayName("场景10: 获取角色列表")
  void testGetRoleList() {
    given()
        .header("Authorization", "Bearer " + accessToken)
        .when()
        .get("/system/role/list")
        .then()
        .statusCode(200)
        .body("code", equalTo(200))
        .body("data.rows", not(empty()));
  }

  @Test
  @Order(11)
  @DisplayName("场景11: 获取部门树")
  void testGetDeptTree() {
    given()
        .header("Authorization", "Bearer " + accessToken)
        .when()
        .get("/system/dept/treeselect")
        .then()
        .statusCode(200)
        .body("code", equalTo(200))
        .body("data", not(empty()));
  }

  @Test
  @Order(12)
  @DisplayName("场景12: 未授权访问应返回401")
  void testUnauthorizedAccess() {
    given().when().get("/system/user/list").then().statusCode(401);
  }

  @Test
  @Order(13)
  @DisplayName("场景13: 无效Token应返回401")
  void testInvalidToken() {
    given()
        .header("Authorization", "Bearer invalid_token_xxx")
        .when()
        .get("/system/user/list")
        .then()
        .statusCode(401);
  }

  @Test
  @Order(14)
  @DisplayName("场景14: 登出")
  void testLogout() {
    given()
        .header("Authorization", "Bearer " + accessToken)
        .when()
        .post("/auth/logout")
        .then()
        .statusCode(200)
        .body("code", equalTo(200));
  }

  @Test
  @Order(15)
  @DisplayName("场景15: 登出后Token失效")
  void testTokenInvalidAfterLogout() {
    given()
        .header("Authorization", "Bearer " + accessToken)
        .when()
        .get("/system/user/getInfo")
        .then()
        .statusCode(401);
  }
}
