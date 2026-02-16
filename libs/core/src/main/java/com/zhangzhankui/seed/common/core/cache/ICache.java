package com.zhangzhankui.seed.common.core.cache;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 缓存接口
 *
 * <p>定义统一的缓存操作规范，支持不同的缓存实现（Redis、Caffeine、Ehcache等）
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface ICache<K, V> {

  /**
   * 获取缓存值
   *
   * @param key 键
   * @return 值，不存在返回 null
   */
  V get(K key);

  /**
   * 获取缓存值，不存在时返回默认值
   *
   * @param key 键
   * @param defaultValue 默认值
   * @return 值
   */
  default V get(K key, V defaultValue) {
    V value = get(key);
    return value != null ? value : defaultValue;
  }

  /**
   * 设置缓存
   *
   * @param key 键
   * @param value 值
   */
  void put(K key, V value);

  /**
   * 设置缓存（带过期时间）
   *
   * @param key 键
   * @param value 值
   * @param timeout 过期时间
   * @param timeUnit 时间单位
   */
  void put(K key, V value, long timeout, TimeUnit timeUnit);

  /**
   * 如果不存在则设置
   *
   * @param key 键
   * @param value 值
   * @return 是否设置成功
   */
  boolean putIfAbsent(K key, V value);

  /**
   * 如果不存在则设置（带过期时间）
   *
   * @param key 键
   * @param value 值
   * @param timeout 过期时间
   * @param timeUnit 时间单位
   * @return 是否设置成功
   */
  boolean putIfAbsent(K key, V value, long timeout, TimeUnit timeUnit);

  /**
   * 删除缓存
   *
   * @param key 键
   * @return 是否删除成功
   */
  boolean remove(K key);

  /**
   * 批量删除
   *
   * @param keys 键集合
   * @return 删除数量
   */
  long remove(Collection<K> keys);

  /**
   * 判断是否存在
   *
   * @param key 键
   * @return 是否存在
   */
  boolean containsKey(K key);

  /**
   * 设置过期时间
   *
   * @param key 键
   * @param timeout 过期时间
   * @param timeUnit 时间单位
   * @return 是否设置成功
   */
  boolean expire(K key, long timeout, TimeUnit timeUnit);

  /**
   * 获取过期时间
   *
   * @param key 键
   * @param timeUnit 时间单位
   * @return 过期时间，-1表示永不过期，-2表示键不存在
   */
  long getExpire(K key, TimeUnit timeUnit);

  /** 清空所有缓存 */
  void clear();

  /**
   * 获取缓存大小
   *
   * @return 缓存数量
   */
  long size();
}
