package com.zhangzhankui.samples.service;

import com.zhangzhankui.samples.common.security.domain.LoginUser;
import com.zhangzhankui.samples.db.entity.User;
import com.zhangzhankui.samples.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 用户详情服务 - 用于Spring Security认证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        if (user.getDeleted()) {
            throw new UsernameNotFoundException("用户已被删除: " + username);
        }

        return buildLoginUser(user);
    }

    /**
     * 构建登录用户信息
     */
    private LoginUser buildLoginUser(User user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(user.getPassword());
        loginUser.setNickname(user.getNickname());
        loginUser.setEmail(user.getEmail());
        loginUser.setPhone(user.getPhone());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setStatus(user.getStatus() != null ? user.getStatus().getValue() : null);

        // 设置角色和权限 (简化示例，实际应从权限表获取)
        loginUser.setRoles(Set.of("USER"));
        loginUser.setPermissions(Set.of("user:read", "user:write"));

        // 管理员特殊处理
        if ("admin".equals(user.getUsername())) {
            loginUser.setRoles(Set.of("ADMIN", "USER"));
            loginUser.setPermissions(Set.of("*:*:*"));
        }

        return loginUser;
    }
}
