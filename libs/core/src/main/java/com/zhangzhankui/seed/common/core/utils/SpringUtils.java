package com.zhangzhankui.seed.common.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** Spring 上下文持有器 提供静态方式获取 Spring Bean */
@Slf4j
@Component
public class SpringUtils implements ApplicationContextAware {

  private static volatile ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(@NonNull ApplicationContext applicationContext)
      throws BeansException {
    SpringUtils.applicationContext = applicationContext;
  }

  /** 获取 ApplicationContext */
  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  /** 通过 name 获取 Bean */
  @SuppressWarnings("unchecked")
  public static <T> T getBean(@NonNull String name) throws BeansException {
    return (T) applicationContext.getBean(name);
  }

  /** 通过 class 获取 Bean */
  public static <T> T getBean(@NonNull Class<T> clazz) throws BeansException {
    return applicationContext.getBean(clazz);
  }

  /** 通过 name 和 class 获取 Bean */
  public static <T> T getBean(@NonNull String name, @NonNull Class<T> clazz) throws BeansException {
    return applicationContext.getBean(name, clazz);
  }

  /** 判断 Bean 是否存在 */
  public static boolean containsBean(@NonNull String name) {
    return applicationContext.containsBean(name);
  }

  /** 判断 Bean 是否为单例 */
  public static boolean isSingleton(@NonNull String name) throws NoSuchBeanDefinitionException {
    return applicationContext.isSingleton(name);
  }

  /** 获取 Bean 的类型 */
  public static Class<?> getType(@NonNull String name) throws NoSuchBeanDefinitionException {
    return applicationContext.getType(name);
  }

  /** 获取 AOP 代理对象 */
  @SuppressWarnings("unchecked")
  public static <T> T getAopProxy() {
    return (T) AopContext.currentProxy();
  }

  /** 获取当前活动的 Profile */
  public static String[] getActiveProfiles() {
    return applicationContext.getEnvironment().getActiveProfiles();
  }

  /** 获取当前活动的 Profile（第一个） */
  public static String getActiveProfile() {
    String[] activeProfiles = getActiveProfiles();
    return activeProfiles.length > 0 ? activeProfiles[0] : null;
  }

  /** 获取配置属性值 */
  public static String getProperty(@NonNull String key) {
    return applicationContext.getEnvironment().getProperty(key);
  }

  /** 获取配置属性值，带默认值 */
  public static String getProperty(@NonNull String key, @NonNull String defaultValue) {
    return applicationContext.getEnvironment().getProperty(key, defaultValue);
  }
}
