package com.zhangzhankui.samples.db.config;

import com.zhangzhankui.samples.common.security.util.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * JPA 审计配置
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> {
            try {
                Long userId = SecurityUtils.getUserId();
                return Optional.ofNullable(userId);
            } catch (Exception e) {
                // 系统操作或未登录时返回空
                return Optional.empty();
            }
        };
    }
}
