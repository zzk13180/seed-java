package com.zhangzhankui.seed.common.web.config;

import brave.baggage.BaggageField;
import brave.baggage.BaggagePropagation;
import brave.baggage.BaggagePropagationConfig.SingleBaggageField;
import brave.propagation.B3Propagation;
import brave.propagation.Propagation;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 链路追踪配置
 *
 * <p>基于 Micrometer Tracing + Brave 实现分布式链路追踪，支持：
 *
 * <ul>
 *   <li>自动生成和传播 TraceId、SpanId
 *   <li>自定义 Baggage 传播（如租户ID、用户ID）
 *   <li>集成 Zipkin 上报
 * </ul>
 *
 * <p>配置示例（application.yml）：
 *
 * <pre>
 * management:
 *   tracing:
 *     enabled: true
 *     sampling:
 *       probability: 1.0  # 采样率（生产环境建议 0.1）
 *   zipkin:
 *     tracing:
 *       endpoint: http://localhost:9411/api/v2/spans
 * </pre>
 */
@Slf4j
@Configuration
@ConditionalOnClass(Tracer.class)
@ConditionalOnProperty(
    name = "management.tracing.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class TracingConfig {

  /** 租户ID Baggage 字段名 */
  public static final String BAGGAGE_TENANT_ID = "tenant-id";

  /** 用户ID Baggage 字段名 */
  public static final String BAGGAGE_USER_ID = "user-id";

  /** 用户名 Baggage 字段名 */
  public static final String BAGGAGE_USERNAME = "username";

  /**
   * 配置 Baggage 传播
   *
   * <p>允许在请求头中传播自定义字段
   */
  @Bean
  public Propagation.Factory propagationFactory() {
    // 定义需要传播的 Baggage 字段
    BaggageField tenantIdField = BaggageField.create(BAGGAGE_TENANT_ID);
    BaggageField userIdField = BaggageField.create(BAGGAGE_USER_ID);
    BaggageField usernameField = BaggageField.create(BAGGAGE_USERNAME);

    return BaggagePropagation.newFactoryBuilder(B3Propagation.FACTORY)
        .add(SingleBaggageField.remote(tenantIdField))
        .add(SingleBaggageField.remote(userIdField))
        .add(SingleBaggageField.remote(usernameField))
        .build();
  }

  /**
   * Tracing 工具类
   *
   * <p>提供静态方法获取和设置追踪信息
   */
  public static class TracingUtils {

    private static Tracer tracer;

    public static void setTracer(Tracer tracer) {
      TracingUtils.tracer = tracer;
    }

    /**
     * 获取当前 TraceId
     *
     * @return TraceId，不存在返回 null
     */
    public static String getTraceId() {
      if (tracer == null || tracer.currentSpan() == null) {
        return null;
      }
      return tracer.currentSpan().context().traceId();
    }

    /**
     * 获取当前 SpanId
     *
     * @return SpanId，不存在返回 null
     */
    public static String getSpanId() {
      if (tracer == null || tracer.currentSpan() == null) {
        return null;
      }
      return tracer.currentSpan().context().spanId();
    }

    /**
     * 添加 Tag 到当前 Span
     *
     * @param key 键
     * @param value 值
     */
    public static void tag(String key, String value) {
      if (tracer != null && tracer.currentSpan() != null) {
        tracer.currentSpan().tag(key, value);
      }
    }

    /**
     * 添加事件到当前 Span
     *
     * @param name 事件名称
     */
    public static void event(String name) {
      if (tracer != null && tracer.currentSpan() != null) {
        tracer.currentSpan().event(name);
      }
    }

    /**
     * 记录错误到当前 Span
     *
     * @param throwable 异常
     */
    public static void error(Throwable throwable) {
      if (tracer != null && tracer.currentSpan() != null) {
        tracer.currentSpan().error(throwable);
      }
    }
  }

  /** 初始化 TracingUtils */
  @Bean
  public TracingUtilsInitializer tracingUtilsInitializer(Tracer tracer) {
    return new TracingUtilsInitializer(tracer);
  }

  /** TracingUtils 初始化器 */
  public static class TracingUtilsInitializer {
    public TracingUtilsInitializer(Tracer tracer) {
      TracingUtils.setTracer(tracer);
      log.info("链路追踪已启用 - Micrometer Tracing with Brave");
    }
  }
}
