package com.zhangzhankui.seed.common.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 操作日志注解 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

  /** 模块名称 */
  String title() default "";

  /** 业务类型 */
  BusinessType businessType() default BusinessType.OTHER;

  /** 操作人类别 */
  OperatorType operatorType() default OperatorType.MANAGE;

  /** 是否保存请求参数 */
  boolean isSaveRequestData() default true;

  /** 是否保存响应数据 */
  boolean isSaveResponseData() default true;

  /** 排除指定的请求参数 */
  String[] excludeParamNames() default {};

  /** 业务类型枚举 */
  enum BusinessType {
    /** 其他 */
    OTHER,
    /** 新增 */
    INSERT,
    /** 修改 */
    UPDATE,
    /** 删除 */
    DELETE,
    /** 授权 */
    GRANT,
    /** 导出 */
    EXPORT,
    /** 导入 */
    IMPORT,
    /** 强退 */
    FORCE,
    /** 清空数据 */
    CLEAN
  }

  /** 操作人类型枚举 */
  enum OperatorType {
    /** 其他 */
    OTHER,
    /** 后台用户 */
    MANAGE,
    /** 手机端用户 */
    MOBILE
  }
}
