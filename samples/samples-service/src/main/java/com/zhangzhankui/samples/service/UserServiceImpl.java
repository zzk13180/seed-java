package com.zhangzhankui.samples.service;

import com.zhangzhankui.samples.api.UserService;
import com.zhangzhankui.samples.api.dto.*;
import com.zhangzhankui.samples.api.vo.UserVO;
import com.zhangzhankui.samples.common.core.domain.PageResult;
import com.zhangzhankui.samples.common.core.enums.UserStatus;
import com.zhangzhankui.samples.common.core.exception.DataAlreadyExistsException;
import com.zhangzhankui.samples.common.core.exception.DataNotFoundException;
import com.zhangzhankui.samples.db.entity.User;
import com.zhangzhankui.samples.db.repository.UserRepository;
import com.zhangzhankui.samples.service.converter.UserConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final PasswordEncoder passwordEncoder;

    private static final String CACHE_NAME = "user";

    @Override
    public PageResult<UserVO> page(UserQueryDTO query) {
        // 构建分页参数
        Sort sort = Sort.by(
                query.isAsc() ? Sort.Direction.ASC : Sort.Direction.DESC,
                StringUtils.hasText(query.getOrderBy()) ? query.getOrderBy() : "createdAt"
        );
        Pageable pageable = PageRequest.of(query.getPageNum() - 1, query.getPageSize(), sort);

        // 执行查询
        // 由于 User 实体使用了 @SQLRestriction("deleted = false")，所有查询会自动过滤已删除记录
        Page<User> page;
        if (StringUtils.hasText(query.getKeyword())) {
            page = userRepository.searchUsers(query.getKeyword(), pageable);
        } else {
            page = userRepository.findAll(pageable);
        }

        // 转换结果
        List<UserVO> list = userConverter.toVOList(page.getContent());
        return PageResult.of(query.getPageNum(), query.getPageSize(), page.getTotalElements(), list);
    }

    @Override
    public List<UserVO> findAll() {
        // 由于 User 实体使用了 @SQLRestriction("deleted = false")，所有查询会自动过滤已删除记录
        List<User> users = userRepository.findAll();
        return userConverter.toVOList(users);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#id", unless = "#result == null")
    public UserVO findById(Long id) {
        // 由于 User 实体使用了 @SQLRestriction("deleted = false")，已删除的用户不会被查出
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("用户", id));
        return userConverter.toVO(user);
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'username:' + #username", unless = "#result == null")
    public UserVO findByUsername(String username) {
        // 由于 User 实体使用了 @SQLRestriction("deleted = false")，已删除的用户不会被查出
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("用户", username));
        return userConverter.toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO create(UserCreateDTO dto) {
        // 检查用户名是否存在
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DataAlreadyExistsException("用户", "username", dto.getUsername());
        }

        // 检查邮箱是否存在
        if (StringUtils.hasText(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new DataAlreadyExistsException("用户", "email", dto.getEmail());
        }

        // 创建用户
        User user = userConverter.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        user = userRepository.save(user);
        log.info("创建用户成功: id={}, username={}", user.getId(), user.getUsername());

        return userConverter.toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = CACHE_NAME, key = "#id"),
            @CacheEvict(value = CACHE_NAME, key = "'username:' + #result.username")
    })
    public UserVO update(Long id, UserUpdateDTO dto) {
        // 由于 User 实体使用了 @SQLRestriction("deleted = false")，已删除的用户不会被查出
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("用户", id));

        // 检查邮箱是否被其他用户使用
        if (StringUtils.hasText(dto.getEmail()) 
                && !dto.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new DataAlreadyExistsException("用户", "email", dto.getEmail());
        }

        // 更新用户
        userConverter.updateEntity(dto, user);
        user = userRepository.save(user);
        log.info("更新用户成功: id={}", id);

        return userConverter.toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = CACHE_NAME, key = "#id"),
            @CacheEvict(value = CACHE_NAME, key = "'username:' + #result")
    })
    public String delete(Long id) {
        // 由于 User 实体使用了 @SQLRestriction("deleted = false")，已删除的用户不会被查出
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("用户", id));
        
        String username = user.getUsername();
        // 使用 deleteById 触发 @SQLDelete 自动执行软删除
        userRepository.deleteById(id);
        log.info("删除用户成功: id={}, username={}", id, username);
        
        return username;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        int rows = userRepository.softDeleteByIds(ids);
        log.info("批量删除用户成功: ids={}, affected={}", ids, rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = CACHE_NAME, key = "#id"),
            @CacheEvict(value = CACHE_NAME, key = "'username:' + #result")
    })
    public String updateStatus(Long id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("用户", id));

        int rows = userRepository.updateStatus(id, status);
        if (rows > 0) {
            log.info("更新用户状态成功: id={}, status={}", id, status);
        }
        return user.getUsername();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = CACHE_NAME, key = "#id"),
            @CacheEvict(value = CACHE_NAME, key = "'username:' + #result")
    })
    public String resetPassword(Long id, ResetPasswordDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("用户", id));

        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());
        int rows = userRepository.updatePassword(id, encodedPassword);
        if (rows > 0) {
            log.info("重置用户密码成功: id={}", id);
        }
        return user.getUsername();
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
