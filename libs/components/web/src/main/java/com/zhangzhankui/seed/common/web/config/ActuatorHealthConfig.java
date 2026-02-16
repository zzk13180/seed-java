package com.zhangzhankui.seed.common.web.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointProperties;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 统一健康检查配置.
 *
 * <p>提供微服务健康检查的统一配置标准，包括：
 * <ul>
 *   <li>健康检查端点暴露</li>
 *   <li>健康检查详情显示策略</li>
 *   <li>自定义健康指示器</li>
 * </ul>
 *
 * <p>配置说明：
 * <pre>
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: health,info,metrics,prometheus
 *   endpoint:
 *     health:
 *       show-details: when_authorized
 *       show-components: when_authorized
 *   health:
 *     defaults:
 *       enabled: true
 * </pre>
 *
 * @author zhangzhankui
 * @since 1.0.0
 */
@AutoConfiguration
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(HealthEndpoint.class)
@EnableConfigurationProperties({HealthEndpointProperties.class, WebEndpointProperties.class})
public class ActuatorHealthConfig {

  /**
   * 应用启动时间健康指示器.
   *
   * @return StartupTimeHealthIndicator
   */
  @Bean
  public StartupTimeHealthIndicator startupTimeHealthIndicator() {
    return new StartupTimeHealthIndicator();
  }
}
