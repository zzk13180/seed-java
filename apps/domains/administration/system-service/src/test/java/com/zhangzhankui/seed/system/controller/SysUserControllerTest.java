package com.zhangzhankui.seed.system.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import com.zhangzhankui.seed.common.core.domain.LoginUser;
import com.zhangzhankui.seed.system.domain.SysUser;
import com.zhangzhankui.seed.system.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * 用户控制器测试 - 使用 MockMvc 进行 Web 层测试
 *
 * <p>只测试 Controller 层逻辑，Service 层被 Mock 使用 Standalone 模式避免 Spring 上下文加载问题
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SysUserController 接口测试")
class SysUserControllerTest {

  private MockMvc mockMvc;

  @Mock private SysUserService userService;

  @Spy private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @InjectMocks private SysUserController userController;

  private SysUser testUser;
  private LoginUser testLoginUser;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

    testUser = new SysUser();
    testUser.setUserId(1L);
    testUser.setUsername("admin");
    testUser.setNickname("管理员");
    testUser.setEmail("admin@example.com");
    testUser.setStatus(1);
    testUser.setDeptId(1L);

    testLoginUser = new LoginUser();
    testLoginUser.setUserId(1L);
    testLoginUser.setUsername("admin");
    testLoginUser.setRoles(Set.of("admin"));
    testLoginUser.setPermissions(Set.of("*:*:*"));
  }

  @Nested
  @DisplayName("GET /system/user/info/{username} - 获取用户信息（内部调用）")
  class GetUserInfoTests {

    @Test
    @DisplayName("应返回用户登录信息")
    void shouldReturnLoginUserInfo() throws Exception {
      // Arrange
      given(userService.getLoginUserByUsername("admin")).willReturn(testLoginUser);

      // Act & Assert
      mockMvc
          .perform(
              get("/system/user/info/admin")
                  .header("X-From-Source", "inner")
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code", is(200)))
          .andExpect(jsonPath("$.data.userId", is(1)))
          .andExpect(jsonPath("$.data.username", is("admin")));
    }

    @Test
    @DisplayName("用户不存在时应返回空数据")
    void shouldReturnNullWhenUserNotExists() throws Exception {
      // Arrange
      given(userService.getLoginUserByUsername("nonexistent")).willReturn(null);

      // Act & Assert
      mockMvc
          .perform(
              get("/system/user/info/nonexistent")
                  .header("X-From-Source", "inner")
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data").doesNotExist());
    }
  }

  @Nested
  @DisplayName("GET /system/user/{userId} - 获取用户详情")
  class GetUserDetailTests {

    @Test
    @DisplayName("应返回用户详情")
    void shouldReturnUserDetail() throws Exception {
      // Arrange
      given(userService.getById(1L)).willReturn(testUser);

      // Act & Assert
      mockMvc
          .perform(get("/system/user/1").contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code", is(200)))
          .andExpect(jsonPath("$.data.username", is("admin")))
          .andExpect(jsonPath("$.data.nickname", is("管理员")));
    }
  }

  @Nested
  @DisplayName("POST /system/user - 新增用户")
  class CreateUserTests {

    @Test
    @DisplayName("应成功创建用户")
    void shouldCreateUserSuccessfully() throws Exception {
      // Arrange - createUser 是 void 方法，不需要 stub 返回值
      // 只需要确保它不抛异常即可

      String requestBody =
          """
          {
              "username": "newuser",
              "password": "Test@123456",
              "nickname": "新用户",
              "email": "new@example.com"
          }
          """;

      // Act & Assert
      mockMvc
          .perform(post("/system/user").contentType(MediaType.APPLICATION_JSON).content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code", is(200)));
    }
  }

  @Nested
  @DisplayName("PUT /system/user - 修改用户")
  class UpdateUserTests {

    @Test
    @DisplayName("应成功更新用户")
    void shouldUpdateUserSuccessfully() throws Exception {
      // Arrange - update 是 void 方法，不需要 stub 返回值

      String requestBody =
          """
          {
              "userId": 1,
              "username": "admin",
              "nickname": "更新后的昵称",
              "email": "updated@example.com"
          }
          """;

      // Act & Assert
      mockMvc
          .perform(put("/system/user").contentType(MediaType.APPLICATION_JSON).content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.code", is(200)));
    }
  }

  @Nested
  @DisplayName("DELETE /system/user/{userIds} - 删除用户")
  class DeleteUserTests {

    @Test
    @DisplayName("应成功删除用户")
    void shouldDeleteUserSuccessfully() throws Exception {
      // Arrange - softDelete 不需要返回值，只需要不抛异常
      // 使用 doNothing() 或者直接不 stub（void 方法默认不做任何事）

      // Act & Assert
      mockMvc.perform(delete("/system/user/2")).andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("输入验证测试")
  class InputValidationTests {

    @Test
    @DisplayName("无效的用户ID格式应返回400")
    void shouldReturn400ForInvalidUserIdFormat() throws Exception {
      mockMvc
          .perform(get("/system/user/invalid").contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("边界条件测试")
  class BoundaryConditionTests {

    @Test
    @DisplayName("超大ID应正确处理")
    void shouldHandleLargeUserId() throws Exception {
      given(userService.getById(Long.MAX_VALUE)).willReturn(null);

      mockMvc
          .perform(get("/system/user/" + Long.MAX_VALUE).contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("创建用户时缺少必填字段应返回400")
    void shouldFailWhenMissingRequiredFields() throws Exception {
      String requestBody =
          """
          {
              "email": "incomplete@example.com"
          }
          """;

      // 没有username应该返回400错误
      mockMvc
          .perform(post("/system/user").contentType(MediaType.APPLICATION_JSON).content(requestBody))
          .andExpect(status().isBadRequest());
    }
  }
}
