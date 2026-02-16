package com.zhangzhankui.seed.common.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/** 限流注解 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

  /** 限流key */
  String key() default "";

  /** 限流时间，默认60秒 */
  int time() default 60;

  /** 限流次数，默认100次 */
  int count() default 100;

  /** 限流类型 */
  LimitType limitType() default LimitType.DEFAULT;

  /** 时间单位 */
  TimeUnit timeUnit() default TimeUnit.SECONDS;

  /** 提示信息 */
  String message() default "访问过于频繁，请稍后重试";

  /** 限流类型枚举 */
  enum LimitType {
    /** 默认策略全局限流 */
    DEFAULT,
    /** 根据请求者IP进行限流 */
    IP,
    /** 根据用户ID进行限流 */
    USER
  }
}
