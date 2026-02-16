package com.zhangzhankui.seed.auth.domain.vo;

import com.zhangzhankui.seed.common.core.domain.LoginUser;

/**
 * 登录响应 VO
 *
 * @param token 访问令牌
 * @param tokenName 令牌名称（如 Authorization）
 * @param user 用户信息
 */
public record LoginVO(String token, String tokenName, LoginUser user) {

  /**
   * 兼容构造器（不含用户信息）
   */
  public LoginVO(String token, String tokenName) {
    this(token, tokenName, null);
  }
}
