package com.zhangzhankui.seed.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/** 系统服务启动类 */
@EnableDiscoveryClient
@MapperScan("com.zhangzhankui.seed.system.mapper")
@SpringBootApplication(scanBasePackages = {"com.zhangzhankui.seed.system", "com.zhangzhankui.seed.common"})
public class SeedSystemApplication {

  public static void main(String[] args) {
    SpringApplication.run(SeedSystemApplication.class, args);
  }
}
