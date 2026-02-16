package com.zhangzhankui.seed.common.test;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试执行时间监控扩展
 *
 * <p>记录每个测试方法的执行时间，并在执行时间过长时发出警告
 */
public class TestExecutionTimeExtension
    implements BeforeTestExecutionCallback, AfterTestExecutionCallback, TestWatcher {

  private static final Logger log = LoggerFactory.getLogger(TestExecutionTimeExtension.class);
  private static final String START_TIME = "start_time";

  // 警告阈值（毫秒）
  private static final long WARNING_THRESHOLD_MS = 1000;
  // 严重警告阈值（毫秒）
  private static final long CRITICAL_THRESHOLD_MS = 5000;

  @Override
  public void beforeTestExecution(ExtensionContext context) {
    context.getStore(ExtensionContext.Namespace.GLOBAL).put(START_TIME, Instant.now());
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    Instant startTime =
        context.getStore(ExtensionContext.Namespace.GLOBAL).remove(START_TIME, Instant.class);

    if (startTime != null) {
      long durationMs = Duration.between(startTime, Instant.now()).toMillis();
      String testName = getTestName(context);

      if (durationMs > CRITICAL_THRESHOLD_MS) {
        log.warn(
            "⚠️ CRITICAL: Test '{}' took {} ms (threshold: {} ms)",
            testName,
            durationMs,
            CRITICAL_THRESHOLD_MS);
      } else if (durationMs > WARNING_THRESHOLD_MS) {
        log.warn(
            "⚠️ SLOW: Test '{}' took {} ms (threshold: {} ms)",
            testName,
            durationMs,
            WARNING_THRESHOLD_MS);
      } else {
        log.debug("✓ Test '{}' completed in {} ms", testName, durationMs);
      }
    }
  }

  @Override
  public void testSuccessful(ExtensionContext context) {
    log.info("✅ PASSED: {}", getTestName(context));
  }

  @Override
  public void testFailed(ExtensionContext context, Throwable cause) {
    log.error("❌ FAILED: {} - {}", getTestName(context), cause.getMessage());
  }

  @Override
  public void testAborted(ExtensionContext context, Throwable cause) {
    log.warn("⏭️ ABORTED: {} - {}", getTestName(context), cause.getMessage());
  }

  @Override
  public void testDisabled(ExtensionContext context, java.util.Optional<String> reason) {
    log.info("⏸️ DISABLED: {} - {}", getTestName(context), reason.orElse("No reason"));
  }

  private String getTestName(ExtensionContext context) {
    return context.getTestMethod().map(Method::getName).orElse(context.getDisplayName());
  }
}
