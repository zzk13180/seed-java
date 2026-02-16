package com.zhangzhankui.seed.common.core.context;

/**
 * 用户上下文接口
 *
 * <p>提供当前登录用户信息的抽象，用于解耦业务模块与认证模块。 由 common-security 模块提供具体实现。
 */
public interface UserContextHolder {

  /**
   * 获取当前登录用户ID
   *
   * @return 用户ID，未登录返回 null
   */
  Long getUserId();

  /**
   * 获取当前登录用户名
   *
   * @return 用户名，未登录返回 null
   */
  String getUsername();

  /**
   * 获取当前登录用户昵称
   *
   * @return 昵称，未登录返回 null
   */
  String getNickname();

  /**
   * 获取当前租户ID
   *
   * @return 租户ID，未登录返回 null
   */
  String getTenantId();

  /**
   * 判断当前是否已登录
   *
   * @return true-已登录，false-未登录
   */
  boolean isLogin();
}
