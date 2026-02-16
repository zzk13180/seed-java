package com.zhangzhankui.seed.common.test;

import java.util.Collection;
import java.util.function.Predicate;

import org.mockito.ArgumentMatcher;

/**
 * 自定义 Mockito ArgumentMatchers
 *
 * <p>提供更灵活的参数匹配器，用于复杂的 Mock 验证场景
 */
public final class CustomArgumentMatchers {

  private CustomArgumentMatchers() {
    // 工具类不应实例化
  }

  /** 匹配包含指定子字符串的字符串 */
  public static ArgumentMatcher<String> containsString(String substring) {
    return argument -> argument != null && argument.contains(substring);
  }

  /** 匹配以指定前缀开头的字符串 */
  public static ArgumentMatcher<String> startsWith(String prefix) {
    return argument -> argument != null && argument.startsWith(prefix);
  }

  /** 匹配以指定后缀结尾的字符串 */
  public static ArgumentMatcher<String> endsWith(String suffix) {
    return argument -> argument != null && argument.endsWith(suffix);
  }

  /** 匹配符合正则表达式的字符串 */
  public static ArgumentMatcher<String> matchesRegex(String regex) {
    return argument -> argument != null && argument.matches(regex);
  }

  /** 匹配在指定范围内的数字 */
  public static <T extends Comparable<T>> ArgumentMatcher<T> inRange(T min, T max) {
    return argument ->
        argument != null && argument.compareTo(min) >= 0 && argument.compareTo(max) <= 0;
  }

  /** 匹配集合大小 */
  public static <T> ArgumentMatcher<Collection<T>> hasSize(int expectedSize) {
    return argument -> argument != null && argument.size() == expectedSize;
  }

  /** 匹配非空集合 */
  public static <T> ArgumentMatcher<Collection<T>> isNotEmpty() {
    return argument -> argument != null && !argument.isEmpty();
  }

  /** 匹配包含指定元素的集合 */
  public static <T> ArgumentMatcher<Collection<T>> containsElement(T element) {
    return argument -> argument != null && argument.contains(element);
  }

  /** 匹配满足自定义条件的对象 */
  public static <T> ArgumentMatcher<T> matching(Predicate<T> predicate, String description) {
    return new ArgumentMatcher<>() {
      @Override
      public boolean matches(T argument) {
        return argument != null && predicate.test(argument);
      }

      @Override
      public String toString() {
        return description;
      }
    };
  }

  /** 匹配非空字符串 */
  public static ArgumentMatcher<String> isNotBlank() {
    return argument -> argument != null && !argument.trim().isEmpty();
  }

  /** 匹配指定长度范围的字符串 */
  public static ArgumentMatcher<String> hasLengthBetween(int min, int max) {
    return argument -> argument != null && argument.length() >= min && argument.length() <= max;
  }

  /** 匹配邮箱格式 */
  public static ArgumentMatcher<String> isValidEmail() {
    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    return argument -> argument != null && argument.matches(emailRegex);
  }

  /** 匹配手机号格式（中国大陆） */
  public static ArgumentMatcher<String> isValidPhoneNumber() {
    String phoneRegex = "^1[3-9]\\d{9}$";
    return argument -> argument != null && argument.matches(phoneRegex);
  }

  /** 组合多个匹配器（AND 逻辑） */
  @SafeVarargs
  public static <T> ArgumentMatcher<T> allOf(ArgumentMatcher<T>... matchers) {
    return argument -> {
      for (ArgumentMatcher<T> matcher : matchers) {
        if (!matcher.matches(argument)) {
          return false;
        }
      }
      return true;
    };
  }

  /** 组合多个匹配器（OR 逻辑） */
  @SafeVarargs
  public static <T> ArgumentMatcher<T> anyOf(ArgumentMatcher<T>... matchers) {
    return argument -> {
      for (ArgumentMatcher<T> matcher : matchers) {
        if (matcher.matches(argument)) {
          return true;
        }
      }
      return false;
    };
  }
}
