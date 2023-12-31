package com.zhangzhankui.samples.controller;

import com.zhangzhankui.samples.common.security.util.LoginAttemptLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 应用启动和基础接口测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DisplayName("应用集成测试")
class ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoginAttemptLimiter loginAttemptLimiter;

    @Test
    @DisplayName("应用启动成功")
    void contextLoads() {
        // 应用能够正常启动即表示测试通过
    }

    @Test
    @DisplayName("首页接口返回成功")
    void indexEndpoint_ReturnsOk() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("健康检查端点可访问")
    void healthEndpoint_ReturnsOk() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("登录接口 - 无效凭证返回错误")
    void login_InvalidCredentials_ReturnsError() throws Exception {
        // Mock 限流器
        when(loginAttemptLimiter.isBlocked(anyString())).thenReturn(false);
        when(loginAttemptLimiter.getRemainingAttempts(anyString())).thenReturn(4);

        String loginJson = """
                {
                    "username": "nonexistent",
                    "password": "wrongpassword"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("未授权访问受保护资源返回401")
    void protectedEndpoint_WithoutAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Swagger UI 可访问")
    void swaggerUi_ReturnsOk() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("OpenAPI 文档可访问")
    void openApiDocs_ReturnsOk() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}
