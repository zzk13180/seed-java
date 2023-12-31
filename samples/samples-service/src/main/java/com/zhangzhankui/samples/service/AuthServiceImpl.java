package com.zhangzhankui.samples.service;

import com.zhangzhankui.samples.api.AuthService;
import com.zhangzhankui.samples.api.dto.LoginDTO;
import com.zhangzhankui.samples.api.vo.LoginVO;
import com.zhangzhankui.samples.api.vo.UserVO;
import com.zhangzhankui.samples.common.core.enums.ResponseEnum;
import com.zhangzhankui.samples.common.core.exception.BusinessException;
import com.zhangzhankui.samples.common.security.config.SecurityProperties;
import com.zhangzhankui.samples.common.security.domain.LoginUser;
import com.zhangzhankui.samples.common.security.util.JwtUtils;
import com.zhangzhankui.samples.common.security.util.LoginAttemptLimiter;
import com.zhangzhankui.samples.common.security.util.SecurityUtils;
import com.zhangzhankui.samples.db.entity.User;
import com.zhangzhankui.samples.db.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 认证服务实现
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SecurityProperties securityProperties;
    private final UserRepository userRepository;
    private final LoginAttemptLimiter loginAttemptLimiter;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtils jwtUtils, SecurityProperties securityProperties, UserRepository userRepository, LoginAttemptLimiter loginAttemptLimiter) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.securityProperties = securityProperties;
        this.userRepository = userRepository;
        this.loginAttemptLimiter = loginAttemptLimiter;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        String username = dto.getUsername();
        
        // 检查是否被锁定
        if (loginAttemptLimiter.isBlocked(username)) {
            long remainingSeconds = loginAttemptLimiter.getRemainingLockTime(username);
            long remainingMinutes = (remainingSeconds + 59) / 60;
            log.warn("用户登录被锁定: username={}, 剩余时间={}分钟", username, remainingMinutes);
            throw new BusinessException(ResponseEnum.ACCOUNT_LOCKED, 
                    String.format("登录失败次数过多，请%d分钟后再试", remainingMinutes));
        }
        
        try {
            // 认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, dto.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();

            // 登录成功，清除失败记录
            loginAttemptLimiter.clearFailedAttempts(username);

            // 生成Token
            String accessToken = jwtUtils.generateToken(loginUser.getUsername());
            String refreshToken = jwtUtils.generateRefreshToken(loginUser.getUsername());

            // 构建响应
            LoginVO vo = new LoginVO();
            vo.setAccessToken(accessToken);
            vo.setRefreshToken(refreshToken);
            vo.setExpiresIn(securityProperties.getJwtExpiration());
            vo.setUser(buildUserVO(loginUser));

            log.info("用户登录成功: username={}", username);
            return vo;

        } catch (DisabledException e) {
            log.warn("用户账号被禁用: username={}", username);
            throw new BusinessException(ResponseEnum.ACCOUNT_DISABLED);
        } catch (BadCredentialsException e) {
            // 记录登录失败
            loginAttemptLimiter.recordFailedAttempt(username);
            int remaining = loginAttemptLimiter.getRemainingAttempts(username);
            
            log.warn("用户密码错误: username={}, 剩余尝试次数={}", username, remaining);
            
            if (remaining > 0) {
                throw new BusinessException(ResponseEnum.PASSWORD_ERROR, 
                        String.format("密码错误，还剩%d次尝试机会", remaining));
            } else {
                throw new BusinessException(ResponseEnum.ACCOUNT_LOCKED, 
                        "登录失败次数过多，账号已被临时锁定");
            }
        } catch (Exception e) {
            log.error("登录失败: username={}, error={}", username, e.getMessage());
            throw new BusinessException(ResponseEnum.UNAUTHORIZED, "登录失败: " + e.getMessage());
        }
    }

    @Override
    public void logout() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null) {
            log.info("用户登出: username={}", loginUser.getUsername());
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(ResponseEnum.TOKEN_INVALID);
        }

        String username = jwtUtils.extractUsername(refreshToken);
        
        // 检查 username 是否有效
        if (username == null || username.isBlank()) {
            throw new BusinessException(ResponseEnum.TOKEN_INVALID, "Token 中缺少用户信息");
        }
        
        // 生成新Token
        String newAccessToken = jwtUtils.generateToken(username);
        String newRefreshToken = jwtUtils.generateRefreshToken(username);

        // 获取用户信息
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResponseEnum.ACCOUNT_NOT_FOUND));

        LoginVO vo = new LoginVO();
        vo.setAccessToken(newAccessToken);
        vo.setRefreshToken(newRefreshToken);
        vo.setExpiresIn(securityProperties.getJwtExpiration());
        vo.setUser(buildUserVO(user));

        return vo;
    }

    @Override
    public LoginVO getCurrentUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(ResponseEnum.UNAUTHORIZED);
        }

        LoginVO vo = new LoginVO();
        vo.setUser(buildUserVO(loginUser));
        return vo;
    }

    private UserVO buildUserVO(LoginUser loginUser) {
        UserVO vo = new UserVO();
        vo.setId(loginUser.getUserId());
        vo.setUsername(loginUser.getUsername());
        vo.setNickname(loginUser.getNickname());
        vo.setEmail(loginUser.getEmail());
        vo.setPhone(loginUser.getPhone());
        vo.setAvatar(loginUser.getAvatar());
        vo.setStatus(loginUser.getStatus());
        vo.setRoles(loginUser.getRoles());
        vo.setPermissions(loginUser.getPermissions());
        return vo;
    }

    private UserVO buildUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus() != null ? user.getStatus().getValue() : null);
        vo.setCreatedAt(user.getCreatedAt());
        // 简化示例，实际应从权限表获取
        vo.setRoles(Set.of("USER"));
        vo.setPermissions(Set.of());
        return vo;
    }
}
