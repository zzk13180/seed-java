package com.zhangzhankui.seed.common.core.context;

/**
 * 默认用户上下文实现（空实现）
 *
 * <p>当没有引入 security 模块时使用此默认实现， 避免空指针异常。
 */
public class DefaultUserContextHolder implements UserContextHolder {

  @Override
  public Long getUserId() {
    return null;
  }

  @Override
  public String getUsername() {
    return null;
  }

  @Override
  public String getNickname() {
    return null;
  }

  @Override
  public String getTenantId() {
    return null;
  }

  @Override
  public boolean isLogin() {
    return false;
  }
}
