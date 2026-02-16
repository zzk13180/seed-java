package com.zhangzhankui.seed.common.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/** 防重复提交注解 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {

  /** 间隔时间（ms），默认5000毫秒 */
  int interval() default 5000;

  /** 时间单位 */
  TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

  /** 提示信息 */
  String message() default "不允许重复提交，请稍后重试";
}
