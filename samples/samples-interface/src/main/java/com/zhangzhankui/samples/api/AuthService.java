package com.zhangzhankui.samples.api;

import com.zhangzhankui.samples.api.dto.LoginDTO;
import com.zhangzhankui.samples.api.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 登出
     */
    void logout();

    /**
     * 刷新Token
     */
    LoginVO refreshToken(String refreshToken);

    /**
     * 获取当前用户信息
     */
    LoginVO getCurrentUser();
}
