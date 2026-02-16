package com.zhangzhankui.seed.system.api;

import com.zhangzhankui.seed.common.core.domain.ApiResult;
import com.zhangzhankui.seed.common.core.domain.LoginUser;
import com.zhangzhankui.seed.system.api.dto.SysUserDTO;
import com.zhangzhankui.seed.system.api.dto.UserCredentialsDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 用户服务远程调用接口
 *
 * <p>基于 Spring 6.1+ HTTP Interface，供其他微服务调用系统服务
 */
@HttpExchange("/system/user")
public interface RemoteUserService {

  /**
   * 通过用户名获取登录用户信息
   *
   * @param username 用户名
   * @return 登录用户信息
   */
  @GetExchange("/info/{username}")
  ApiResult<LoginUser> getUserInfo(@PathVariable("username") String username);

  /**
   * 获取用户凭据信息（含密码哈希，仅限内部调用）
   *
   * <p>auth-service 使用此方法获取用户凭据，在本地完成密码验证， 避免明文密码通过 RPC 传输。
   *
   * @param username 用户名
   * @return 用户凭据信息（含 BCrypt 密码哈希）
   */
  @GetExchange("/credentials/{username}")
  ApiResult<UserCredentialsDTO> getUserCredentials(@PathVariable("username") String username);

  /**
   * 通过用户ID获取登录用户信息
   *
   * @param userId 用户ID
   * @return 登录用户信息
   */
  @GetExchange("/info/id/{userId}")
  ApiResult<LoginUser> getUserInfoById(@PathVariable("userId") Long userId);

  /**
   * 创建用户（内部调用）
   *
   * @param dto 用户信息
   * @return 创建结果
   */
  @PostExchange
  ApiResult<Void> createUser(@RequestBody SysUserDTO dto);

  /**
   * 创建 OAuth2 用户（内部调用，无需密码）
   *
   * @param dto 用户信息
   * @return 创建结果
   */
  @PostExchange("/oauth2")
  ApiResult<Void> createOAuth2User(@RequestBody SysUserDTO dto);
}
