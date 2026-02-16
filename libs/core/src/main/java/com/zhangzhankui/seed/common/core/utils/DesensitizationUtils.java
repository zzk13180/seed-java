package com.zhangzhankui.seed.common.core.utils;

import org.springframework.util.StringUtils;

/**
 * 数据脱敏工具类
 *
 * <p>提供常见的数据脱敏功能，如手机号、身份证号、邮箱等
 */
public class DesensitizationUtils {

  private DesensitizationUtils() {
    // 工具类，禁止实例化
  }

  /**
   * 手机号脱敏
   *
   * <p>保留前3位和后4位，中间用*号替换
   *
   * <p>例如：138****1234
   *
   * @param phone 手机号
   * @return 脱敏后的手机号
   */
  public static String desensitizePhone(String phone) {
    if (!StringUtils.hasText(phone) || phone.length() != 11) {
      return phone;
    }
    return phone.substring(0, 3) + "****" + phone.substring(7);
  }

  /**
   * 身份证号脱敏
   *
   * <p>保留前6位和后4位，中间用*号替换
   *
   * @param idCard 身份证号
   * @return 脱敏后的身份证号
   */
  public static String desensitizeIdCard(String idCard) {
    if (!StringUtils.hasText(idCard) || idCard.length() < 8) {
      return idCard;
    }
    return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
  }

  /**
   * 邮箱脱敏
   *
   * <p>保留@前面的第一个字符和@后面的域名，中间用*号替换
   *
   * <p>例如：a***@example.com
   *
   * @param email 邮箱
   * @return 脱敏后的邮箱
   */
  public static String desensitizeEmail(String email) {
    if (!StringUtils.hasText(email) || !email.contains("@")) {
      return email;
    }
    int atIndex = email.indexOf("@");
    String prefix = email.substring(0, atIndex);
    String suffix = email.substring(atIndex);

    if (prefix.length() <= 1) {
      return "*" + suffix;
    }

    return prefix.charAt(0) + "*".repeat(prefix.length() - 1) + suffix;
  }

  /**
   * 银行卡号脱敏
   *
   * <p>保留前4位和后4位，中间用*号替换
   *
   * @param bankCard 银行卡号
   * @return 脱敏后的银行卡号
   */
  public static String desensitizeBankCard(String bankCard) {
    if (!StringUtils.hasText(bankCard) || bankCard.length() < 8) {
      return bankCard;
    }
    return bankCard.substring(0, 4)
        + "*".repeat(Math.max(0, bankCard.length() - 8))
        + bankCard.substring(bankCard.length() - 4);
  }
}
