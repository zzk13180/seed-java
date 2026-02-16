package com.zhangzhankui.seed.common.core.constant;

/** 缓存常量 */
public interface CacheConstants {

  /** 登录账户密码错误次数 redis key */
  String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

  /** 登录IP错误次数 redis key */
  String LOGIN_ERROR_KEY = "login_error:";

  /** 限流 redis key */
  String RATE_LIMIT_KEY = "rate_limit:";

  /** 防重提交 redis key */
  String REPEAT_SUBMIT_KEY = "repeat_submit:";

  /** 用户登录缓存 redis key */
  String USER_LOGIN_KEY = "user:login:";
}
