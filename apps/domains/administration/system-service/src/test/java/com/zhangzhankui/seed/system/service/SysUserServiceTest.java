package com.zhangzhankui.seed.system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.zhangzhankui.seed.common.core.domain.LoginUser;
import com.zhangzhankui.seed.common.core.domain.PageQuery;
import com.zhangzhankui.seed.common.core.domain.PageResult;
import com.zhangzhankui.seed.common.core.exception.ServiceException;
import com.zhangzhankui.seed.system.api.vo.SysUserQueryVO;
import com.zhangzhankui.seed.system.domain.SysUser;
import com.zhangzhankui.seed.system.repository.SysUserRepository;
import com.zhangzhankui.seed.system.service.impl.SysUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 用户服务单元测试
 *
 * <p>遵循 AAA 模式：Arrange（准备）, Act（执行）, Assert（断言）
 *
 * <p>新架构下 Service 层依赖 Repository 接口（依赖注入），
 * 测试时只需 Mock Repository 即可，无需反射操作。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SysUserService 单元测试")
class SysUserServiceTest {

  @Mock private SysUserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private SysUserServiceImpl userService;

  private SysUser testUser;

  @BeforeEach
  void setUp() {
    // 通过构造器注入 Repository 和 PasswordEncoder
    userService = new SysUserServiceImpl(userRepository, passwordEncoder);

    testUser = new SysUser();
    testUser.setUserId(1L);
    testUser.setUsername("admin");
    testUser.setNickname("管理员");
    testUser.setEmail("admin@example.com");
    testUser.setPassword("$2a$10$encrypted_password");
    testUser.setDeptId(1L);
    testUser.setStatus(1);
    testUser.setDeleted(0);
  }

  @Nested
  @DisplayName("findByUsername - 根据用户名查询用户")
  class FindByUsernameTests {

    @Test
    @DisplayName("用户存在时应返回正确的用户信息")
    void shouldReturnUserWhenUsernameExists() {
      // Arrange
      given(userRepository.findByUsername("admin")).willReturn(Optional.of(testUser));

      // Act
      SysUser result = userService.findByUsername("admin");

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo("admin");
      assertThat(result.getNickname()).isEqualTo("管理员");
      verify(userRepository).findByUsername("admin");
    }

    @Test
    @DisplayName("用户不存在时应返回null")
    void shouldReturnNullWhenUsernameNotExists() {
      // Arrange
      given(userRepository.findByUsername("nonexistent")).willReturn(Optional.empty());

      // Act
      SysUser result = userService.findByUsername("nonexistent");

      // Assert
      assertThat(result).isNull();
    }
  }

  @Nested
  @DisplayName("getLoginUserByUsername - 获取登录用户信息")
  class GetLoginUserByUsernameTests {

    @Test
    @DisplayName("应返回完整的登录用户信息（包含角色和权限）")
    void shouldReturnLoginUserWithRolesAndPermissions() {
      // Arrange
      given(userRepository.findByUsername("admin")).willReturn(Optional.of(testUser));
      given(userRepository.findRoleKeysByUserId(1L)).willReturn(Set.of("admin"));
      given(userRepository.findPermissionsByUserId(1L))
          .willReturn(Set.of("system:user:list", "system:user:add"));

      // Act
      LoginUser loginUser = userService.getLoginUserByUsername("admin");

      // Assert
      assertThat(loginUser).isNotNull();
      assertThat(loginUser.getUserId()).isEqualTo(1L);
      assertThat(loginUser.getUsername()).isEqualTo("admin");
      assertThat(loginUser.getRoles()).contains("admin");
      assertThat(loginUser.getPermissions()).contains("*:*:*"); // 管理员应有所有权限
    }

    @Test
    @DisplayName("普通用户应只有分配的权限")
    void shouldReturnNormalUserPermissions() {
      // Arrange
      testUser.setUsername("user");
      given(userRepository.findByUsername("user")).willReturn(Optional.of(testUser));
      given(userRepository.findRoleKeysByUserId(1L)).willReturn(Set.of("user"));
      given(userRepository.findPermissionsByUserId(1L)).willReturn(Set.of("system:user:list"));

      // Act
      LoginUser loginUser = userService.getLoginUserByUsername("user");

      // Assert
      assertThat(loginUser).isNotNull();
      assertThat(loginUser.getRoles()).contains("user");
      assertThat(loginUser.getPermissions()).doesNotContain("*:*:*");
      assertThat(loginUser.getPermissions()).contains("system:user:list");
    }

    @Test
    @DisplayName("用户不存在时应返回null")
    void shouldReturnNullWhenUserNotExists() {
      // Arrange
      given(userRepository.findByUsername("nonexistent")).willReturn(Optional.empty());

      // Act
      LoginUser loginUser = userService.getLoginUserByUsername("nonexistent");

      // Assert
      assertThat(loginUser).isNull();
    }
  }

  @Nested
  @DisplayName("findById - 根据ID查询用户")
  class FindByIdTests {

    @Test
    @DisplayName("应返回正确的用户信息")
    void shouldReturnUserWhenIdExists() {
      // Arrange
      given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

      // Act
      Optional<SysUser> result = userService.findById(1L);

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get().getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("ID不存在时应返回empty")
    void shouldReturnEmptyWhenIdNotExists() {
      // Arrange
      given(userRepository.findById(999L)).willReturn(Optional.empty());

      // Act
      Optional<SysUser> result = userService.findById(999L);

      // Assert
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getById - 根据ID查询用户（不存在时抛异常）")
  class GetByIdTests {

    @Test
    @DisplayName("应返回正确的用户信息")
    void shouldReturnUserWhenIdExists() {
      // Arrange
      given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

      // Act
      SysUser result = userService.getById(1L);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("ID不存在时应抛出 ServiceException")
    void shouldThrowExceptionWhenIdNotExists() {
      // Arrange
      given(userRepository.findById(999L)).willReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> userService.getById(999L))
          .isInstanceOf(ServiceException.class)
          .hasMessageContaining("不存在");
    }
  }

  @Nested
  @DisplayName("create - 创建用户")
  class CreateUserTests {

    @Test
    @DisplayName("应成功创建新用户")
    void shouldCreateUserSuccessfully() {
      // Arrange
      SysUser newUser = new SysUser();
      newUser.setUsername("newuser");
      newUser.setPassword("password");
      given(userRepository.save(newUser)).willReturn(newUser);

      // Act
      SysUser result = userService.create(newUser);

      // Assert
      assertThat(result).isNotNull();
      verify(userRepository).save(newUser);
    }
  }

  @Nested
  @DisplayName("update - 更新用户")
  class UpdateUserTests {

    @Test
    @DisplayName("应成功更新用户信息")
    void shouldUpdateUserSuccessfully() {
      // Arrange
      testUser.setNickname("新昵称");
      given(userRepository.update(testUser)).willReturn(testUser);

      // Act
      SysUser result = userService.update(testUser);

      // Assert
      assertThat(result).isNotNull();
      verify(userRepository).update(testUser);
    }
  }

  @Nested
  @DisplayName("deleteById - 删除用户")
  class DeleteUserTests {

    @Test
    @DisplayName("应成功删除用户")
    void shouldDeleteUserSuccessfully() {
      // Arrange
      given(userRepository.deleteById(2L)).willReturn(true);

      // Act
      userService.deleteById(2L);

      // Assert
      verify(userRepository).deleteById(2L);
    }
  }

  @Nested
  @DisplayName("queryUserPage - 分页查询")
  class QueryUserPageTests {

    @Test
    @DisplayName("应返回分页结果")
    void shouldReturnPageResult() {
      // Arrange
      SysUserQueryVO query = new SysUserQueryVO(null, null, null, null, null);
      PageQuery pageQuery = new PageQuery();
      pageQuery.setPageNum(1);
      pageQuery.setPageSize(10);

      PageResult<SysUser> expectedResult = PageResult.of(1, 10, 1, List.of(testUser));
      given(userRepository.findByCondition(query, pageQuery)).willReturn(expectedResult);

      // Act
      PageResult<SysUser> result = userService.queryUserPage(query, pageQuery);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getTotal()).isEqualTo(1);
      assertThat(result.getRecords()).hasSize(1);
    }
  }

  @Nested
  @DisplayName("边界条件测试")
  class BoundaryConditionTests {

    @Test
    @DisplayName("用户名为null时findByUsername应返回null")
    void shouldReturnNullWhenUsernameIsNull() {
      // Arrange
      given(userRepository.findByUsername(null)).willReturn(Optional.empty());

      // Act
      SysUser result = userService.findByUsername(null);

      // Assert
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("existsByUsername 应正确检查用户名")
    void shouldCheckUsernameExists() {
      // Arrange
      given(userRepository.existsByUsername("admin")).willReturn(true);
      given(userRepository.existsByUsername("newuser")).willReturn(false);

      // Act & Assert
      assertThat(userService.existsByUsername("admin")).isTrue();
      assertThat(userService.existsByUsername("newuser")).isFalse();
    }
  }
}
