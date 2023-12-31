package com.zhangzhankui.samples.common.security.domain;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户信息
 */
@Data
public class LoginUser implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 账号状态 (0禁用 1启用)
     */
    private Integer status;

    /**
     * 角色集合
     */
    private Set<String> roles;

    /**
     * 权限集合
     */
    private Set<String> permissions;

    // 手动覆盖 getter/setter 以防止可变对象暴露
    public Set<String> getRoles() {
        return roles != null ? Collections.unmodifiableSet(roles) : Collections.emptySet();
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }

    public Set<String> getPermissions() {
        return permissions != null ? Collections.unmodifiableSet(permissions) : Collections.emptySet();
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions != null ? new HashSet<>(permissions) : new HashSet<>();
    }

    /**
     * Token
     */
    private String token;

    /**
     * 刷新Token
     */
    private String refreshToken;

    /**
     * Token过期时间 (毫秒时间戳)
     */
    private Long expireTime;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 登录IP
     */
    private String loginIp;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将角色和权限合并为Spring Security的权限
        Set<String> authorities = new HashSet<>();
        
        if (roles != null) {
            roles.forEach(role -> authorities.add("ROLE_" + role));
        }
        if (permissions != null) {
            authorities.addAll(permissions);
        }
        
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.status != null && this.status == 1;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }
}
