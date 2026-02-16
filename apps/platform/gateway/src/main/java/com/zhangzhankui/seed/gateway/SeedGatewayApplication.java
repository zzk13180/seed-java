package com.zhangzhankui.seed.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/** 网关服务启动类 */
@EnableDiscoveryClient
@SpringBootApplication
public class SeedGatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(SeedGatewayApplication.class, args);
  }
}
