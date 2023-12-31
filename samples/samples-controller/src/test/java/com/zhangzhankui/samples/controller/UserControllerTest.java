package com.zhangzhankui.samples.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangzhankui.samples.api.UserService;
import com.zhangzhankui.samples.api.dto.ResetPasswordDTO;
import com.zhangzhankui.samples.api.dto.UserCreateDTO;
import com.zhangzhankui.samples.api.vo.UserVO;
import com.zhangzhankui.samples.common.core.domain.PageResult;
import com.zhangzhankui.samples.common.core.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户控制器集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("用户控制器测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("分页查询用户 - 需要认证")
    void page_RequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    @DisplayName("分页查询用户 - 成功")
    void page_Success() throws Exception {
        // Given
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setUsername("testuser");
        
        PageResult<UserVO> pageResult = PageResult.of(1, 10, 1L, Collections.singletonList(userVO));
        when(userService.page(any())).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/v1/users")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(authorities = "user:read")
    @DisplayName("根据ID查询用户 - 成功")
    void getById_Success() throws Exception {
        // Given
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setUsername("testuser");
        when(userService.findById(1L)).thenReturn(userVO);

        // When & Then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @WithMockUser(authorities = "user:write")
    @DisplayName("创建用户 - 成功")
    void create_Success() throws Exception {
        // Given
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("newuser");
        createDTO.setPassword("password123");
        createDTO.setEmail("new@example.com");

        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setUsername("newuser");
        when(userService.create(any())).thenReturn(userVO);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    @Test
    @WithMockUser(authorities = "user:write")
    @DisplayName("创建用户 - 参数校验失败")
    void create_ValidationFailed() throws Exception {
        // Given
        UserCreateDTO createDTO = new UserCreateDTO();
        // 缺少必填字段

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("删除用户 - 成功")
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @WithMockUser(authorities = "user:read")
    @DisplayName("删除用户 - 权限不足")
    void delete_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("更新用户状态 - 成功")
    void updateStatus_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/users/1/status")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @WithMockUser
    @DisplayName("检查用户名是否存在")
    void existsByUsername() throws Exception {
        when(userService.existsByUsername("testuser")).thenReturn(true);

        mockMvc.perform(get("/api/v1/users/exists/username")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("重置密码 - 成功")
    void resetPassword_Success() throws Exception {
        // Given
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setNewPassword("newPass123");
        dto.setConfirmPassword("newPass123");

        // When & Then
        mockMvc.perform(patch("/api/v1/users/1/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("重置密码 - 密码不匹配")
    void resetPassword_PasswordMismatch() throws Exception {
        // Given
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setNewPassword("newPass123");
        dto.setConfirmPassword("differentPass");

        // When & Then
        mockMvc.perform(patch("/api/v1/users/1/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("重置密码 - 密码长度不足")
    void resetPassword_PasswordTooShort() throws Exception {
        // Given
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setNewPassword("123");
        dto.setConfirmPassword("123");

        // When & Then
        mockMvc.perform(patch("/api/v1/users/1/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    @DisplayName("重置密码 - 权限不足")
    void resetPassword_Forbidden() throws Exception {
        // Given
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setNewPassword("newPass123");
        dto.setConfirmPassword("newPass123");

        // When & Then
        mockMvc.perform(patch("/api/v1/users/1/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
