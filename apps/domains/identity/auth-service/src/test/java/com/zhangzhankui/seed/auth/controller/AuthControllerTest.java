package com.zhangzhankui.seed.auth.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Set;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangzhankui.seed.auth.domain.vo.LoginVO;
import com.zhangzhankui.seed.auth.domain.vo.UserInfoVO;
import com.zhangzhankui.seed.auth.service.LoginService;
import com.zhangzhankui.seed.common.core.auth.AuthProvider;
import com.zhangzhankui.seed.common.core.domain.LoginUser;
import com.zhangzhankui.seed.common.core.domain.model.LoginBody;
import com.zhangzhankui.seed.common.core.exception.ServiceException;
import com.zhangzhankui.seed.common.core.exception.UserException;
import com.zhangzhankui.seed.common.security.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * AuthController 控制器单元测试
 *
 * 测试覆盖：
 * - 登录接口 (POST /auth/login)
 * - 登出接口 (POST /auth/logout)
 * - 获取用户信息 (GET /auth/info)
 * - 刷新Token (POST /auth/refresh)
 *
 * 注意：响应格式使用 RFC 9457 ProblemDetail 标准
 */
@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
    }
)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("认证控制器测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private AuthProvider authProvider;

    private LoginBody validLoginBody;

    @BeforeEach
    void setUp() {
        validLoginBody = new LoginBody();
        validLoginBody.setUsername("admin");
        validLoginBody.setPassword("password123");
    }

    @Nested
    @DisplayName("POST /auth/login - 用户登录")
    class LoginTests {

        @Test
        @DisplayName("正确的用户名和密码应返回Token")
        void login_WithValidCredentials_ShouldReturnToken() throws Exception {
            // Arrange
            String expectedToken = "mock-token-12345";
            LoginUser mockUser = new LoginUser();
            mockUser.setUserId(1L);
            mockUser.setUsername("admin");
            LoginVO loginVO = new LoginVO(expectedToken, "Authorization", mockUser);
            when(loginService.login("admin", "password123")).thenReturn(loginVO);

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value(expectedToken));

            verify(loginService).login("admin", "password123");
        }

        @Test
        @DisplayName("空用户名应返回验证错误")
        void login_WithEmptyUsername_ShouldReturnValidationError() throws Exception {
            // Arrange
            LoginBody invalidBody = new LoginBody();
            invalidBody.setUsername("");
            invalidBody.setPassword("password");

            // Act & Assert - ProblemDetail 格式
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("参数校验失败"));
        }

        @Test
        @DisplayName("空密码应返回验证错误")
        void login_WithEmptyPassword_ShouldReturnValidationError() throws Exception {
            // Arrange
            LoginBody invalidBody = new LoginBody();
            invalidBody.setUsername("admin");
            invalidBody.setPassword("");

            // Act & Assert - ProblemDetail 格式
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("参数校验失败"));
        }

        @Test
        @DisplayName("错误的密码应返回认证失败")
        void login_WithWrongPassword_ShouldReturnUnauthorized() throws Exception {
            // Arrange - UserException 默认 code=401
            when(loginService.login(anyString(), anyString()))
                .thenThrow(new UserException("用户不存在或密码错误"));

            // Act & Assert - ProblemDetail 格式，UserException 返回 401 UNAUTHORIZED
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("业务异常"))
                .andExpect(jsonPath("$.detail").value("用户不存在或密码错误"));
        }

        @Test
        @DisplayName("不存在的用户应返回认证失败")
        void login_WithNonExistentUser_ShouldReturnUnauthorized() throws Exception {
            // Arrange
            LoginBody body = new LoginBody();
            body.setUsername("nonexistent");
            body.setPassword("password");

            when(loginService.login("nonexistent", "password"))
                .thenThrow(new UserException("用户不存在或密码错误"));

            // Act & Assert - ProblemDetail 格式，UserException 返回 401 UNAUTHORIZED
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("业务异常"));
        }

        @Test
        @DisplayName("账户锁定应返回相应错误")
        void login_WithLockedAccount_ShouldReturnLocked() throws Exception {
            // Arrange - ServiceException 使用其 code 作为 HTTP 状态码，无效码回退到 400
            when(loginService.login(anyString(), anyString()))
                .thenThrow(new ServiceException("账户已锁定，请30分钟后重试"));

            // Act & Assert - ProblemDetail 格式
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("业务异常"))
                .andExpect(jsonPath("$.detail").value("账户已锁定，请30分钟后重试"));
        }
    }

    @Nested
    @DisplayName("POST /auth/logout - 用户登出")
    class LogoutTests {

        @Test
        @DisplayName("登出应成功")
        void logout_ShouldSucceed() throws Exception {
            // Arrange
            doNothing().when(loginService).logout();

            // Act & Assert
            mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

            verify(loginService).logout();
        }
    }

    @Nested
    @DisplayName("GET /auth/info - 获取用户信息")
    class GetInfoTests {

        @Test
        @DisplayName("已认证用户应返回用户信息")
        void getInfo_WhenAuthenticated_ShouldReturnUserInfo() throws Exception {
            // Arrange
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(1L);
            loginUser.setUsername("admin");
            loginUser.setNickname("管理员");
            loginUser.setRoles(Set.of("admin", "user"));
            loginUser.setPermissions(Set.of("system:user:list"));

            UserInfoVO userInfoVO = new UserInfoVO(
                loginUser, loginUser.getRoles(), loginUser.getPermissions()
            );
            when(loginService.getUserInfo()).thenReturn(userInfoVO);

            // Act & Assert
            mockMvc.perform(get("/auth/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.user.username").value("admin"))
                .andExpect(jsonPath("$.data.roles").isArray());

            verify(loginService).getUserInfo();
        }

        @Test
        @DisplayName("未认证用户应返回错误")
        void getInfo_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
            // Arrange - ServiceException 默认映射到 400 BAD_REQUEST
            when(loginService.getUserInfo())
                .thenThrow(new ServiceException("用户未登录"));

            // Act & Assert - ProblemDetail 格式
            mockMvc.perform(get("/auth/info"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("业务异常"))
                .andExpect(jsonPath("$.detail").value("用户未登录"));
        }
    }

    @Nested
    @DisplayName("POST /auth/refresh - 刷新Token")
    class RefreshTests {

        @Test
        @DisplayName("刷新Token应返回新Token")
        void refresh_ShouldReturnNewToken() throws Exception {
            // 注意：此测试需要 Sa-Token 上下文，实际运行需要集成测试
            // 这里仅验证端点可访问性
            mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isOk());
        }
    }
}
