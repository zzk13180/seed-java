package com.zhangzhankui.seed.common.web.config;

import java.time.Duration;
import java.time.Instant;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * 启动时间健康指示器.
 *
 * <p>显示应用启动时间和运行时长，用于监控和诊断。
 *
 * @author zhangzhankui
 * @since 1.0.0
 */
public class StartupTimeHealthIndicator implements HealthIndicator {

  private final Instant startupTime;

  public StartupTimeHealthIndicator() {
    this.startupTime = Instant.now();
  }

  @Override
  public Health health() {
    Duration uptime = Duration.between(startupTime, Instant.now());
    return Health.up()
        .withDetail("startupTime", startupTime.toString())
        .withDetail("uptime", formatDuration(uptime))
        .withDetail("uptimeSeconds", uptime.getSeconds())
        .build();
  }

  private String formatDuration(Duration duration) {
    long days = duration.toDays();
    long hours = duration.toHours() % 24;
    long minutes = duration.toMinutes() % 60;
    long seconds = duration.getSeconds() % 60;

    if (days > 0) {
      return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    } else if (hours > 0) {
      return String.format("%dh %dm %ds", hours, minutes, seconds);
    } else if (minutes > 0) {
      return String.format("%dm %ds", minutes, seconds);
    } else {
      return String.format("%ds", seconds);
    }
  }
}
