package com.zhangzhankui.seed.common.test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 测试数据生成器
 *
 * <p>提供各种测试数据的随机生成功能
 */
public final class TestDataGenerator {

  private static final Random RANDOM = ThreadLocalRandom.current();

  private static final String[] FIRST_NAMES = {"张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴"};
  private static final String[] LAST_NAMES = {"伟", "芳", "娜", "强", "敏", "静", "磊", "洋", "艳", "勇"};
  private static final String[] EMAIL_DOMAINS = {
    "@gmail.com", "@qq.com", "@163.com", "@outlook.com"
  };

  private TestDataGenerator() {
    // 工具类不应实例化
  }

  // ==================== 基础类型生成 ====================

  /** 生成随机字符串 */
  public static String randomString(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
    }
    return sb.toString();
  }

  /** 生成随机整数 */
  public static int randomInt(int min, int max) {
    return RANDOM.nextInt(max - min + 1) + min;
  }

  /** 生成随机长整数 */
  public static long randomLong(long min, long max) {
    return RANDOM.nextLong(max - min + 1) + min;
  }

  /** 生成随机布尔值 */
  public static boolean randomBoolean() {
    return RANDOM.nextBoolean();
  }

  // ==================== 业务数据生成 ====================

  /** 生成随机用户名 */
  public static String randomUsername() {
    return "user_" + randomString(8).toLowerCase();
  }

  /** 生成随机中文姓名 */
  public static String randomChineseName() {
    return FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)]
        + LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)]
        + LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
  }

  /** 生成随机邮箱 */
  public static String randomEmail() {
    return randomString(10).toLowerCase() + EMAIL_DOMAINS[RANDOM.nextInt(EMAIL_DOMAINS.length)];
  }

  /** 生成随机手机号（中国大陆） */
  public static String randomPhoneNumber() {
    String[] prefixes = {"138", "139", "150", "151", "152", "158", "159", "188", "189"};
    return prefixes[RANDOM.nextInt(prefixes.length)]
        + String.format("%08d", RANDOM.nextInt(100000000));
  }

  /** 生成随机密码（符合常见密码策略） */
  public static String randomPassword() {
    return "Test@" + randomString(6) + randomInt(10, 99);
  }

  /** 生成随机 IP 地址 */
  public static String randomIpAddress() {
    return String.format(
        "%d.%d.%d.%d", randomInt(1, 255), randomInt(0, 255), randomInt(0, 255), randomInt(1, 254));
  }

  /** 生成随机 UUID */
  public static String randomUuid() {
    return UUID.randomUUID().toString();
  }

  // ==================== 日期时间生成 ====================

  /** 生成过去N天内的随机日期时间 */
  public static LocalDateTime randomPastDateTime(int maxDaysAgo) {
    int daysAgo = randomInt(0, maxDaysAgo);
    int hoursAgo = randomInt(0, 23);
    int minutesAgo = randomInt(0, 59);
    return LocalDateTime.now().minusDays(daysAgo).minusHours(hoursAgo).minusMinutes(minutesAgo);
  }

  /** 生成未来N天内的随机日期时间 */
  public static LocalDateTime randomFutureDateTime(int maxDaysAhead) {
    int daysAhead = randomInt(1, maxDaysAhead);
    int hoursAhead = randomInt(0, 23);
    int minutesAhead = randomInt(0, 59);
    return LocalDateTime.now().plusDays(daysAhead).plusHours(hoursAhead).plusMinutes(minutesAhead);
  }

  // ==================== 集合生成 ====================

  /** 生成随机字符串列表 */
  public static List<String> randomStringList(int size, int stringLength) {
    List<String> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      list.add(randomString(stringLength));
    }
    return list;
  }

  /** 从数组中随机选择一个元素 */
  @SafeVarargs
  public static <T> T randomChoice(T... options) {
    return options[RANDOM.nextInt(options.length)];
  }

  /** 从列表中随机选择一个元素 */
  public static <T> T randomChoice(List<T> options) {
    return options.get(RANDOM.nextInt(options.size()));
  }

  // ==================== 对象生成 ====================

  /** 填充对象的字段（使用随机值） 支持 String, Integer, Long, Boolean, LocalDateTime 类型 */
  public static <T> T populateRandomly(T obj) {
    try {
      for (Field field : obj.getClass().getDeclaredFields()) {
        field.setAccessible(true);
        Class<?> type = field.getType();

        if (type == String.class) {
          field.set(obj, randomString(10));
        } else if (type == Integer.class || type == int.class) {
          field.set(obj, randomInt(1, 100));
        } else if (type == Long.class || type == long.class) {
          field.set(obj, randomLong(1, 10000));
        } else if (type == Boolean.class || type == boolean.class) {
          field.set(obj, randomBoolean());
        } else if (type == LocalDateTime.class) {
          field.set(obj, randomPastDateTime(30));
        }
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Failed to populate object", e);
    }
    return obj;
  }
}
