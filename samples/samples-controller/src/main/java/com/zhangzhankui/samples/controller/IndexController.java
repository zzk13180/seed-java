package com.zhangzhankui.samples.controller;

import com.zhangzhankui.samples.common.core.controller.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器
 */
@Slf4j
@Tag(name = "系统信息", description = "系统基本信息接口")
@RestController
public class IndexController {

    @Value("${spring.application.name:seed-java}")
    private String applicationName;

    @Value("${app.version:0.0.1-SNAPSHOT}")
    private String appVersion;

    @Operation(summary = "首页")
    @GetMapping("/")
    public ResponseMessage<Map<String, Object>> index() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", applicationName);
        info.put("version", appVersion);
        info.put("time", LocalDateTime.now());
        info.put("message", "Welcome to " + applicationName + " API");
        return ResponseMessage.ok(info);
    }

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public ResponseMessage<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("time", LocalDateTime.now());
        return ResponseMessage.ok(health);
    }

    @Operation(summary = "Hello接口")
    @GetMapping("/index/hello")
    public ResponseMessage<String> hello() {
        log.info("Hello world");
        return ResponseMessage.ok("Hello, World! 🚀");
    }
}
