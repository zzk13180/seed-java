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
 * System 服务作为 Provider 的契约验证测试
 *
 * <p>验证 System 服务是否满足与 Auth 服务的契约
 *
 * <p>运行方式：
 * 1. 确保测试环境已启动 (docker-compose --profile test up -d)
 * 2. 确保 auth 模块已生成 Pact 文件 (target/pacts/*.json)
 * 3. 将 Pact 文件复制到本模块的 src/test/resources/pacts/ 目录
 * 4. 运行: mvn test -Dgroups=contract -pl modules/system
 *
 * <p>注意：此测试使用真实数据库验证契约，数据库中需要有测试数据
 */
@Provider("system-service")
@PactFolder("pacts")
@IgnoreNoPactsToVerify
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("contract")
class SystemProviderContractTest {

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

  /**
   * 状态：用户 admin 存在
   * 测试数据库中应该已有 admin 用户
   */
  @State("用户 admin 存在")
  void setupAdminUserExists() {
    // 使用真实数据库中的 admin 用户
    // 确保数据库中有 admin 用户及其角色和权限
  }

  /**
   * 状态：用户 nonexistent 不存在
   * 测试数据库中不应该有此用户
   */
  @State("用户 nonexistent 不存在")
  void setupUserNotExists() {
    // 不需要特殊设置，数据库中没有 nonexistent 用户
  }

  /** 状态：缺少内部认证头 */
  @State("缺少内部认证头")
  void setupMissingInnerAuth() {
    // 不需要特殊设置，由安全过滤器处理
  }
}
