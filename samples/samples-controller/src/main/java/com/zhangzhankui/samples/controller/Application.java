package com.zhangzhankui.samples.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 应用启动类
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "com.zhangzhankui.samples")
@EntityScan(basePackages = "com.zhangzhankui.samples.db.entity")
@EnableJpaRepositories(basePackages = "com.zhangzhankui.samples.db.repository")
@ConfigurationPropertiesScan(basePackages = "com.zhangzhankui.samples")
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        printStartupInfo(context);
    }

    private static void printStartupInfo(ConfigurableApplicationContext context) {
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String appName = env.getProperty("spring.application.name", "seed-java");
        
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("无法获取主机地址: {}", e.getMessage());
        }

        log.info("""
                
                ----------------------------------------------------------
                  应用 '{}' 启动成功!
                ----------------------------------------------------------
                  本地地址:    http://localhost:{}{}
                  网络地址:    http://{}:{}{}
                  API文档:     http://localhost:{}{}/swagger-ui.html
                  Actuator:    http://localhost:{}{}/actuator
                ----------------------------------------------------------
                """,
                appName,
                port, contextPath,
                hostAddress, port, contextPath,
                port, contextPath,
                port, contextPath
        );
    }
}
