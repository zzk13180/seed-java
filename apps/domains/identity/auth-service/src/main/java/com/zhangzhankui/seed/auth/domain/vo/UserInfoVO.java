package com.zhangzhankui.seed.auth.domain.vo;

import java.util.Set;

import com.zhangzhankui.seed.common.core.domain.LoginUser;

/**
 * 用户信息响应 VO
 *
 * @param user 用户信息
 * @param roles 角色集合
 * @param permissions 权限集合
 */
public record UserInfoVO(LoginUser user, Set<String> roles, Set<String> permissions) {}
