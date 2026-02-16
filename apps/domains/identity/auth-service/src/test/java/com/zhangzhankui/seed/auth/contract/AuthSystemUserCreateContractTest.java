package com.zhangzhankui.seed.auth.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Auth 服务 -> System 服务 契约测试（用户创建场景）
 *
 * <p>覆盖 RemoteUserService 中的 createOAuth2User 和 getUserInfoById 接口。
 * Auth 服务作为 Consumer，System 服务作为 Provider。
 *
 * <p>运行方式：mvn test -Dtest.groups="contract" -pl apps/domains/identity/auth-service
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "system-service")
@Tag("contract")
@DisplayName("Auth -> System 用户创建契约测试")
class AuthSystemUserCreateContractTest {

  private final RestTemplate restTemplate = new RestTemplate();

  // ======================== 契约定义 ========================

  /** 契约：通过 ID 获取用户信息成功 */
  @Pact(consumer = "auth-service", provider = "system-service")
  public V4Pact getUserByIdSuccess(PactDslWithProvider builder) {
    return builder
        .given("用户 ID 1 存在")
        .uponReceiving("通过 ID 获取用户信息的请求")
        .path("/user/info/id/1")
        .method("GET")
        .headers(Map.of("X-From-Source", "inner", "Content-Type", "application/json"))
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            new PactDslJsonBody()
                .integerType("code", 200)
                .stringType("message", "操作成功")
                .object("data")
                .integerType("userId", 1)
                .stringType("username", "admin")
                .integerType("deptId", 1)
                .array("roles")
                .stringType("admin")
                .closeArray()
                .array("permissions")
                .stringType("*:*:*")
                .closeArray()
                .closeObject())
        .toPact(V4Pact.class);
  }

  /** 契约：通过 ID 获取用户信息 - 用户不存在 */
  @Pact(consumer = "auth-service", provider = "system-service")
  public V4Pact getUserByIdNotFound(PactDslWithProvider builder) {
    return builder
        .given("用户 ID 99999 不存在")
        .uponReceiving("通过 ID 获取不存在用户的请求")
        .path("/user/info/id/99999")
        .method("GET")
        .headers(Map.of("X-From-Source", "inner", "Content-Type", "application/json"))
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            new PactDslJsonBody()
                .integerType("code", 200)
                .stringType("message", "操作成功")
                .nullValue("data"))
        .toPact(V4Pact.class);
  }

  /** 契约：创建 OAuth2 用户成功 */
  @Pact(consumer = "auth-service", provider = "system-service")
  public V4Pact createOAuth2UserSuccess(PactDslWithProvider builder) {
    return builder
        .given("可以创建新的 OAuth2 用户")
        .uponReceiving("创建 OAuth2 用户的请求")
        .path("/user/oauth2")
        .method("POST")
        .headers(Map.of("X-From-Source", "inner", "Content-Type", "application/json"))
        .body(
            new PactDslJsonBody()
                .stringType("username", "oauth2_user")
                .stringType("password", "encrypted_password")
                .stringType("nickname", "OAuth2 测试用户")
                .stringType("email", "oauth2@example.com")
                .integerType("deptId", 1)
                .integerType("status", 1))
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            new PactDslJsonBody()
                .integerType("code", 200)
                .stringType("message", "操作成功"))
        .toPact(V4Pact.class);
  }

  /** 契约：创建 OAuth2 用户 - 用户名已存在 */
  @Pact(consumer = "auth-service", provider = "system-service")
  public V4Pact createOAuth2UserDuplicate(PactDslWithProvider builder) {
    return builder
        .given("用户 admin 已存在")
        .uponReceiving("创建已存在用户名的 OAuth2 用户请求")
        .path("/user/oauth2")
        .method("POST")
        .headers(Map.of("X-From-Source", "inner", "Content-Type", "application/json"))
        .body(
            new PactDslJsonBody()
                .stringType("username", "admin")
                .stringType("password", "encrypted_password")
                .stringType("nickname", "重复用户")
                .integerType("deptId", 1)
                .integerType("status", 1))
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            new PactDslJsonBody()
                .integerType("code", 500)
                .stringType("message", "用户名已存在"))
        .toPact(V4Pact.class);
  }

  // ======================== 测试方法 ========================

  @Test
  @PactTestFor(pactMethod = "getUserByIdSuccess")
  @DisplayName("通过 ID 成功获取用户信息")
  void shouldGetUserInfoByIdSuccessfully(MockServer mockServer) {
    // Arrange
    String url = mockServer.getUrl() + "/user/info/id/1";
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-From-Source", "inner");
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Act
    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("userId");
    assertThat(response.getBody()).contains("admin");
  }

  @Test
  @PactTestFor(pactMethod = "getUserByIdNotFound")
  @DisplayName("通过 ID 查找不存在的用户返回空数据")
  void shouldReturnNullWhenUserIdNotFound(MockServer mockServer) {
    // Arrange
    String url = mockServer.getUrl() + "/user/info/id/99999";
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-From-Source", "inner");
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Act
    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("\"data\":null");
  }

  @Test
  @PactTestFor(pactMethod = "createOAuth2UserSuccess")
  @DisplayName("成功创建 OAuth2 用户")
  void shouldCreateOAuth2UserSuccessfully(MockServer mockServer) {
    // Arrange
    String url = mockServer.getUrl() + "/user/oauth2";
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-From-Source", "inner");
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body =
        """
        {
          "username": "oauth2_user",
          "password": "encrypted_password",
          "nickname": "OAuth2 测试用户",
          "email": "oauth2@example.com",
          "deptId": 1,
          "status": 1
        }
        """;

    // Act
    ResponseEntity<String> response =
        restTemplate.exchange(
            url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("200");
  }

  @Test
  @PactTestFor(pactMethod = "createOAuth2UserDuplicate")
  @DisplayName("创建已存在用户名的 OAuth2 用户返回失败")
  void shouldFailWhenDuplicateOAuth2User(MockServer mockServer) {
    // Arrange
    String url = mockServer.getUrl() + "/user/oauth2";
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-From-Source", "inner");
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body =
        """
        {
          "username": "admin",
          "password": "encrypted_password",
          "nickname": "重复用户",
          "deptId": 1,
          "status": 1
        }
        """;

    // Act
    ResponseEntity<String> response =
        restTemplate.exchange(
            url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("用户名已存在");
  }
}
