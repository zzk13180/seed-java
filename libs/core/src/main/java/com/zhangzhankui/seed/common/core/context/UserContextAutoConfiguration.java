package com.zhangzhankui.seed.common.core.context;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** 用户上下文自动配置 */
@AutoConfiguration
public class UserContextAutoConfiguration {

  /** 默认用户上下文（当没有其他实现时使用） */
  @Bean
  @ConditionalOnMissingBean(UserContextHolder.class)
  public UserContextHolder defaultUserContextHolder() {
    return new DefaultUserContextHolder();
  }
}
