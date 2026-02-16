package com.zhangzhankui.seed.system.api.dto;

import java.util.Set;

/**
 * 用户凭据信息 DTO（仅限内部服务间使用）
 *
 * <p>包含密码哈希，用于 auth-service 在本地验证密码。 避免明文密码通过 RPC 在微服务间传输。
 *
 * <p>安全说明：此 DTO 仅通过 {@code @InnerAuth} 保护的内部端点返回， 并由 HMAC 签名保证请求来源可信。
 */
public record UserCredentialsDTO(
    Long userId,
    String username,
    String nickname,
    String passwordHash,
    Integer status,
    Long deptId,
    Set<String> roles,
    Set<String> permissions) {}
