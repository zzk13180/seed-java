package com.zhangzhankui.seed.auth.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Auth 服务 -> System 服务 契约测试
 *
 * <p>使用 Pact 框架进行契约驱动开发 (Contract-Driven Development) Auth 服务作为 Consumer，System 服务作为 Provider
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "system-service")
@Tag("contract")
@DisplayName("Auth -> System 服务契约测试")
class AuthSystemContractTest {

  private final RestTemplate restTemplate = new RestTemplate();

  /** 契约：成功获取用户信息 */
  @Pact(consumer = "auth-service", provider = "system-service")
  public V4Pact getUserByUsernameSuccess(PactDslWithProvider builder) {
    return builder
        .given("用户 admin 存在")
        .uponReceiving("获取 admin 用户信息的请求")
        .path("/user/info/admin")
        .method("GET")
        .headers(Map.of("X-From-Source", "inner", "Content-Type", "application/json"))
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            new PactDslJsonBody()
                .integerType("code", 200)
                .stringType("message", "success")
                .object("data")
                .integerType("userId", 1)
                .stringType("username", "admin")
                .stringType("password", "$2a$10$encrypted")
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

  /** 契约：用户不存在 */
  @Pact(consumer = "auth-service", provider = "system-service")
  public V4Pact getUserByUsernameNotFound(PactDslWithProvider builder) {
    return builder
        .given("用户 nonexistent 不存在")
        .uponReceiving("获取不存在用户信息的请求")
        .path("/user/info/nonexistent")
        .method("GET")
        .headers(Map.of("X-From-Source", "inner", "Content-Type", "application/json"))
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            new PactDslJsonBody()
                .integerType("code", 200)
                .stringType("message", "success")
                .nullValue("data"))
        .toPact(V4Pact.class);
  }

  /** 契约：缺少内部认证头 */
  @Pact(consumer = "auth-service", provider = "system-service")
  public V4Pact getMissingInnerAuth(PactDslWithProvider builder) {
    return builder
        .given("缺少内部认证头")
        .uponReceiving("缺少 X-From-Source 头的请求")
        .path("/user/info/admin")
        .method("GET")
        .willRespondWith()
        .status(401)
        .headers(Map.of("Content-Type", "application/json"))
        .body(new PactDslJsonBody().integerType("code", 401).stringType("message", "内部认证失败"))
        .toPact(V4Pact.class);
  }

  @Test
  @PactTestFor(pactMethod = "getUserByUsernameSuccess")
  @DisplayName("成功获取用户信息")
  void shouldGetUserInfoSuccessfully(MockServer mockServer) {
    // Arrange
    String url = mockServer.getUrl() + "/user/info/admin";
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-From-Source", "inner");
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Act
    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

    // Assert
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("admin");
    assertThat(response.getBody()).contains("userId");
  }

  @Test
  @PactTestFor(pactMethod = "getUserByUsernameNotFound")
  @DisplayName("用户不存在时返回空数据")
  void shouldReturnNullWhenUserNotFound(MockServer mockServer) {
    // Arrange
    String url = mockServer.getUrl() + "/user/info/nonexistent";
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
  @PactTestFor(pactMethod = "getMissingInnerAuth")
  @DisplayName("缺少内部认证头时返回401")
  void shouldReturn401WhenMissingInnerAuth(MockServer mockServer) {
    // Arrange
    String url = mockServer.getUrl() + "/user/info/admin";

    // Act & Assert
    assertThatThrownBy(() -> restTemplate.getForEntity(url, String.class))
        .isInstanceOf(HttpClientErrorException.class)
        .hasMessageContaining("401");
  }
}
