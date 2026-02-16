package com.zhangzhankui.seed.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/** 认证服务启动类 */
@EnableDiscoveryClient
@SpringBootApplication(
    scanBasePackages = {"com.zhangzhankui.seed.auth", "com.zhangzhankui.seed.common"},
    exclude = {OAuth2ClientAutoConfiguration.class})
public class SeedAuthApplication {

  public static void main(String[] args) {
    SpringApplication.run(SeedAuthApplication.class, args);
  }
}
