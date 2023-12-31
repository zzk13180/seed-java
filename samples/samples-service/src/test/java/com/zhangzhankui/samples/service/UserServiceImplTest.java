package com.zhangzhankui.samples.service;

import com.zhangzhankui.samples.api.dto.UserCreateDTO;
import com.zhangzhankui.samples.api.dto.UserUpdateDTO;
import com.zhangzhankui.samples.api.vo.UserVO;
import com.zhangzhankui.samples.common.core.enums.UserStatus;
import com.zhangzhankui.samples.common.core.exception.DataAlreadyExistsException;
import com.zhangzhankui.samples.common.core.exception.DataNotFoundException;
import com.zhangzhankui.samples.db.entity.User;
import com.zhangzhankui.samples.db.repository.UserRepository;
import com.zhangzhankui.samples.service.converter.UserConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserConverter userConverter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserVO testUserVO;
    private UserCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setStatus(UserStatus.ENABLED);

        testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setUsername("testuser");
        testUserVO.setEmail("test@example.com");

        createDTO = new UserCreateDTO();
        createDTO.setUsername("newuser");
        createDTO.setPassword("password123");
        createDTO.setEmail("new@example.com");
    }

    @Test
    @DisplayName("根据ID查询用户 - 成功")
    void findById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userConverter.toVO(testUser)).thenReturn(testUserVO);

        // When
        UserVO result = userService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).findById(1L);
        verify(userConverter).toVO(testUser);
    }

    @Test
    @DisplayName("根据ID查询用户 - 用户不存在")
    void findById_NotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessageContaining("用户");
    }

    @Test
    @DisplayName("创建用户 - 成功")
    void create_Success() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userConverter.toEntity(any(UserCreateDTO.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userConverter.toVO(any(User.class))).thenReturn(testUserVO);

        // When
        UserVO result = userService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).existsByUsername(createDTO.getUsername());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(createDTO.getPassword());
    }

    @Test
    @DisplayName("创建用户 - 用户名已存在")
    void create_UsernameExists() {
        // Given
        when(userRepository.existsByUsername(createDTO.getUsername())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.create(createDTO))
                .isInstanceOf(DataAlreadyExistsException.class)
                .hasMessageContaining("用户");
    }

    @Test
    @DisplayName("创建用户 - 邮箱已存在")
    void create_EmailExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(createDTO.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.create(createDTO))
                .isInstanceOf(DataAlreadyExistsException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("更新用户 - 成功")
    void update_Success() {
        // Given
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setNickname("Updated Name");
        updateDTO.setEmail("updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userConverter.toVO(any(User.class))).thenReturn(testUserVO);

        // When
        UserVO result = userService.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
        verify(userConverter).updateEntity(updateDTO, testUser);
    }

    @Test
    @DisplayName("删除用户 - 成功")
    void delete_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        String username = userService.delete(1L);

        // Then
        assertThat(username).isEqualTo("testuser");
        verify(userRepository).findById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("删除用户 - 用户不存在")
    void delete_NotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @DisplayName("检查用户名是否存在")
    void existsByUsername() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        // When & Then
        assertThat(userService.existsByUsername("testuser")).isTrue();
        assertThat(userService.existsByUsername("newuser")).isFalse();
    }

    @Test
    @DisplayName("检查邮箱是否存在")
    void existsByEmail() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        // When & Then
        assertThat(userService.existsByEmail("test@example.com")).isTrue();
        assertThat(userService.existsByEmail("new@example.com")).isFalse();
    }
}
