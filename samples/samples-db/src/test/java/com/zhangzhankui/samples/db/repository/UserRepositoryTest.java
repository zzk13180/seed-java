package com.zhangzhankui.samples.db.repository;

import com.zhangzhankui.samples.common.core.enums.UserStatus;
import com.zhangzhankui.samples.db.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepository 数据访问层测试
 * 
 * 使用 @DataJpaTest 进行轻量级 JPA 测试
 * - 自动配置内存数据库
 * - 自动配置 JPA 相关组件
 * - 测试完成后自动回滚
 * 
 * 注意：User 实体使用了 @SQLRestriction("deleted = false")，
 * 所有通过 Repository 的查询会自动过滤已删除的记录。
 * 
 * @author zhangzhankui
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository 数据访问层测试")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setNickname("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setStatus(UserStatus.ENABLED);
        testUser.setDeleted(false);
        
        entityManager.persistAndFlush(testUser);
    }

    @Nested
    @DisplayName("基础查询测试")
    class BasicQueryTests {

        @Test
        @DisplayName("根据ID查询 - 存在")
        void findById_ExistingUser_ReturnsUser() {
            Optional<User> found = userRepository.findById(testUser.getId());
            
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("根据ID查询 - 不存在")
        void findById_NonExistingUser_ReturnsEmpty() {
            Optional<User> found = userRepository.findById(999L);
            
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("根据用户名查询 - 由于 @SQLRestriction 自动过滤已删除记录")
        void findByUsername_ReturnsUser() {
            Optional<User> found = userRepository.findByUsername("testuser");
            
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("根据邮箱查询")
        void findByEmail_ReturnsUser() {
            Optional<User> found = userRepository.findByEmail("test@example.com");
            
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("软删除测试 - @SQLRestriction 自动过滤")
    class SoftDeleteTests {

        @Test
        @DisplayName("软删除后查询自动过滤（由于 @SQLRestriction）")
        void findById_DeletedUser_ReturnsEmpty() {
            // 软删除用户
            testUser.setDeleted(true);
            entityManager.persistAndFlush(testUser);
            entityManager.clear(); // 清除一级缓存

            // 由于 @SQLRestriction("deleted = false")，已删除用户不会被查出
            Optional<User> found = userRepository.findById(testUser.getId());
            
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("使用 deleteById 触发 @SQLDelete 软删除")
        void deleteById_SoftDeletes() {
            Long userId = testUser.getId();
            
            userRepository.deleteById(userId);
            entityManager.flush();
            entityManager.clear();

            // 由于 @SQLRestriction，findById 不会返回已删除用户
            Optional<User> found = userRepository.findById(userId);
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("分页查询测试")
    class PaginationTests {

        @BeforeEach
        void setUpMoreUsers() {
            for (int i = 2; i <= 15; i++) {
                User user = new User();
                user.setUsername("user" + i);
                user.setPassword("password");
                user.setNickname("用户" + i);
                user.setEmail("user" + i + "@example.com");
                user.setStatus(UserStatus.ENABLED);
                user.setDeleted(false);
                entityManager.persist(user);
            }
            entityManager.flush();
        }

        @Test
        @DisplayName("分页查询 - 第一页（使用 findAll，@SQLRestriction 自动过滤）")
        void findAll_FirstPage_Returns10Items() {
            Page<User> page = userRepository.findAll(
                    PageRequest.of(0, 10, Sort.by("id").ascending())
            );

            assertThat(page.getContent()).hasSize(10);
            assertThat(page.getTotalElements()).isEqualTo(15);
            assertThat(page.getTotalPages()).isEqualTo(2);
            assertThat(page.isFirst()).isTrue();
        }

        @Test
        @DisplayName("分页查询 - 第二页")
        void findAll_SecondPage_Returns5Items() {
            Page<User> page = userRepository.findAll(
                    PageRequest.of(1, 10, Sort.by("id").ascending())
            );

            assertThat(page.getContent()).hasSize(5);
            assertThat(page.isLast()).isTrue();
        }
    }

    @Nested
    @DisplayName("存在性检查测试")
    class ExistsTests {

        @Test
        @DisplayName("用户名存在")
        void existsByUsername_ExistingUsername_ReturnsTrue() {
            boolean exists = userRepository.existsByUsername("testuser");
            
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("用户名不存在")
        void existsByUsername_NonExistingUsername_ReturnsFalse() {
            boolean exists = userRepository.existsByUsername("nonexistent");
            
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("邮箱存在")
        void existsByEmail_ExistingEmail_ReturnsTrue() {
            boolean exists = userRepository.existsByEmail("test@example.com");
            
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("已删除用户不计入存在检查（@SQLRestriction 自动过滤）")
        void existsByUsername_DeletedUser_ReturnsFalse() {
            testUser.setDeleted(true);
            entityManager.persistAndFlush(testUser);
            entityManager.clear();

            boolean exists = userRepository.existsByUsername("testuser");
            
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("搜索测试")
    class SearchTests {

        @BeforeEach
        void setUpSearchData() {
            User user2 = new User();
            user2.setUsername("admin");
            user2.setPassword("password");
            user2.setNickname("管理员");
            user2.setEmail("admin@example.com");
            user2.setStatus(UserStatus.ENABLED);
            user2.setDeleted(false);
            entityManager.persist(user2);

            User user3 = new User();
            user3.setUsername("test2");
            user3.setPassword("password");
            user3.setNickname("测试员2");
            user3.setEmail("test2@example.com");
            user3.setStatus(UserStatus.DISABLED);
            user3.setDeleted(false);
            entityManager.persist(user3);

            entityManager.flush();
        }

        @Test
        @DisplayName("按状态搜索")
        void findAllByStatus_ReturnsMatchingResults() {
            var users = userRepository.findAllByStatus(UserStatus.ENABLED);

            assertThat(users).hasSize(2);
            assertThat(users).allMatch(u -> u.getStatus() == UserStatus.ENABLED);
        }

        @Test
        @DisplayName("关键词搜索 - 用户名")
        void searchUsers_Username_ReturnsMatches() {
            Page<User> page = userRepository.searchUsers(
                    "test", PageRequest.of(0, 10)
            );

            assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("关键词搜索 - 昵称")
        void searchUsers_Nickname_ReturnsMatches() {
            Page<User> page = userRepository.searchUsers(
                    "管理", PageRequest.of(0, 10)
            );

            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getUsername()).isEqualTo("admin");
        }
    }

    @Nested
    @DisplayName("CRUD操作测试")
    class CrudTests {

        @Test
        @DisplayName("保存新用户")
        void save_NewUser_Persists() {
            User newUser = new User();
            newUser.setUsername("newuser");
            newUser.setPassword("password");
            newUser.setNickname("新用户");
            newUser.setStatus(UserStatus.ENABLED);
            newUser.setDeleted(false);

            User saved = userRepository.save(newUser);

            assertThat(saved.getId()).isNotNull();
            assertThat(entityManager.find(User.class, saved.getId())).isNotNull();
        }

        @Test
        @DisplayName("更新用户")
        void save_ExistingUser_Updates() {
            testUser.setNickname("更新后的昵称");
            userRepository.save(testUser);
            entityManager.flush();
            entityManager.clear();

            User found = entityManager.find(User.class, testUser.getId());
            assertThat(found.getNickname()).isEqualTo("更新后的昵称");
        }

        @Test
        @DisplayName("统计活跃用户数（使用 count，@SQLRestriction 自动过滤）")
        void count_ReturnsActiveUsersCount() {
            long count = userRepository.count();
            
            assertThat(count).isEqualTo(1);
        }
    }
}
