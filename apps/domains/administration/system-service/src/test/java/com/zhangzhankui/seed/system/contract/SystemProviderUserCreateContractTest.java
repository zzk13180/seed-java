package com.zhangzhankui.seed.system.contract;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * System 服务 Provider 契约验证 — 用户创建与 ID 查询场景
 *
 * <p>验证 System 服务是否满足 auth-service 关于 getUserInfoById 和 createOAuth2User 的契约。
 *
 * <p>Pact 文件来源：auth-service Consumer 测试运行后自动生成的 JSON，
 * 需要复制到本模块的 src/test/resources/pacts/ 目录。
 *
 * <p>运行方式：mvn test -Dtest.groups="contract" -pl apps/domains/administration/system-service
 */
@Provider("system-service")
@PactFolder("pacts")
@IgnoreNoPactsToVerify
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("contract")
class SystemProviderUserCreateContractTest {

  @LocalServerPort private int port;

  @BeforeEach
  void setUp(PactVerificationContext context) {
    if (context != null) {
      context.setTarget(new HttpTestTarget("localhost", port));
    }
  }

  @TestTemplate
  @ExtendWith(PactVerificationInvocationContextProvider.class)
  void pactVerificationTestTemplate(PactVerificationContext context) {
    if (context != null) {
      context.verifyInteraction();
    }
  }

  // ======================== Provider States ========================

  /** 状态：用户 ID 1 存在 */
  @State("用户 ID 1 存在")
  void setupUserIdOneExists() {
    // 数据库中应已有 userId=1 的 admin 用户（由 seed.sql 初始化）
  }

  /** 状态：用户 ID 99999 不存在 */
  @State("用户 ID 99999 不存在")
  void setupUserIdNotExists() {
    // 不需要特殊设置，数据库中没有 userId=99999
  }

  /** 状态：可以创建新的 OAuth2 用户 */
  @State("可以创建新的 OAuth2 用户")
  void setupCanCreateOAuth2User() {
    // 确保数据库中没有 username=oauth2_user 的记录
    // 如果需要，可在此处通过 Repository 删除残留测试数据
  }

  /** 状态：用户 admin 已存在 */
  @State("用户 admin 已存在")
  void setupAdminAlreadyExists() {
    // 数据库中已有 admin 用户，createOAuth2User 应返回失败
  }
}
