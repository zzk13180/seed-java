package com.zhangzhankui.seed.common.redis.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** Redis 工具类 */
@Component
@RequiredArgsConstructor
public class RedisUtils {

  private final RedisTemplate<String, Object> redisTemplate;

  // ========== 通用操作 ==========

  /** 设置过期时间 */
  public boolean expire(@NonNull String key, long timeout, @NonNull TimeUnit unit) {
    return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
  }

  /** 获取过期时间 */
  public Long getExpire(@NonNull String key, @NonNull TimeUnit unit) {
    return redisTemplate.getExpire(key, unit);
  }

  /** 获取过期时间（秒） */
  public Long getExpire(@NonNull String key) {
    return redisTemplate.getExpire(key, TimeUnit.SECONDS);
  }

  /** 判断 key 是否存在 */
  public boolean hasKey(@NonNull String key) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

  /** 删除 key */
  public boolean delete(@NonNull String key) {
    return Boolean.TRUE.equals(redisTemplate.delete(key));
  }

  /** 批量删除 key */
  public Long delete(@NonNull Collection<String> keys) {
    if (keys == null) {
      throw new IllegalArgumentException("keys cannot be null");
    }
    return redisTemplate.delete(keys);
  }

  /** 按模式删除 key */
  public Long deleteByPattern(@NonNull String pattern) {
    Set<String> keys = redisTemplate.keys(pattern);
    if (keys != null && !keys.isEmpty()) {
      return redisTemplate.delete(keys);
    }
    return 0L;
  }

  // ========== String 操作 ==========

  /** 获取值 */
  @SuppressWarnings("unchecked")
  public <T> T get(@NonNull String key) {
    return (T) redisTemplate.opsForValue().get(key);
  }

  /** 设置值 */
  public void set(@NonNull String key, @NonNull Object value) {
    if (key == null) {
      throw new IllegalArgumentException("key cannot be null");
    }
    redisTemplate.opsForValue().set(key, value);
  }

  /** 设置值并指定过期时间 */
  public void set(
      @NonNull String key, @NonNull Object value, long timeout, @NonNull TimeUnit unit) {
    redisTemplate.opsForValue().set(key, value, timeout, unit);
  }

  /** 如果不存在则设置 */
  public boolean setIfAbsent(
      @NonNull String key, @NonNull Object value, long timeout, @NonNull TimeUnit unit) {
    return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit));
  }

  /** 递增 */
  public Long increment(@NonNull String key) {
    return redisTemplate.opsForValue().increment(key);
  }

  /** 递增指定步长 */
  public Long increment(@NonNull String key, long delta) {
    return redisTemplate.opsForValue().increment(key, delta);
  }

  /** 递减 */
  public Long decrement(@NonNull String key) {
    return redisTemplate.opsForValue().decrement(key);
  }

  /**
   * 原子获取并删除值（GETDEL 命令）
   *
   * <p>此方法是原子操作，用于一次性 token 等场景，防止并发重放攻击
   */
  @SuppressWarnings("unchecked")
  public <T> T getAndDelete(@NonNull String key) {
    return (T) redisTemplate.opsForValue().getAndDelete(key);
  }

  // ========== Hash 操作 ==========

  /** 获取 Hash 值 */
  @SuppressWarnings("unchecked")
  public <T> T hGet(@NonNull String key, @NonNull String hashKey) {
    return (T) redisTemplate.opsForHash().get(key, hashKey);
  }

  /** 设置 Hash 值 */
  public void hSet(@NonNull String key, @NonNull String hashKey, @NonNull Object value) {
    if (key == null) {
      throw new IllegalArgumentException("key cannot be null");
    }
    redisTemplate.opsForHash().put(key, hashKey, value);
  }

  /** 批量设置 Hash 值 */
  public void hSetAll(@NonNull String key, @NonNull Map<String, Object> map) {
    redisTemplate.opsForHash().putAll(key, map);
  }

  /** 获取整个 Hash */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T> Map<String, T> hGetAll(@NonNull String key) {
    Map entries = redisTemplate.opsForHash().entries(key);
    return (Map<String, T>) entries;
  }

  /** 删除 Hash 中的值 */
  public Long hDelete(@NonNull String key, @NonNull Object... hashKeys) {
    return redisTemplate.opsForHash().delete(key, hashKeys);
  }

  /** 判断 Hash 中是否存在 */
  public boolean hHasKey(@NonNull String key, @NonNull String hashKey) {
    return redisTemplate.opsForHash().hasKey(key, hashKey);
  }

  // ========== List 操作 ==========

  /** 获取 List 范围 */
  public List<Object> lRange(@NonNull String key, long start, long end) {
    return redisTemplate.opsForList().range(key, start, end);
  }

  /** List 右侧添加 */
  public Long lRightPush(@NonNull String key, @NonNull Object value) {
    if (key == null) {
      throw new IllegalArgumentException("key cannot be null");
    }
    return redisTemplate.opsForList().rightPush(key, value);
  }

  /** List 左侧弹出 */
  public Object lLeftPop(@NonNull String key) {
    return redisTemplate.opsForList().leftPop(key);
  }

  /** 获取 List 长度 */
  public Long lSize(@NonNull String key) {
    return redisTemplate.opsForList().size(key);
  }

  // ========== Set 操作 ==========

  /** 添加到 Set */
  public Long sAdd(@NonNull String key, @NonNull Object... values) {
    if (key == null) {
      throw new IllegalArgumentException("key cannot be null");
    }
    if (values == null) {
      throw new IllegalArgumentException("values cannot be null");
    }
    return redisTemplate.opsForSet().add(key, values);
  }

  /** 获取 Set 成员 */
  public Set<Object> sMembers(@NonNull String key) {
    return redisTemplate.opsForSet().members(key);
  }

  /** 判断是否是 Set 成员 */
  public boolean sIsMember(@NonNull String key, @NonNull Object value) {
    return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
  }

  /** 从 Set 移除 */
  public Long sRemove(@NonNull String key, @NonNull Object... values) {
    return redisTemplate.opsForSet().remove(key, values);
  }
}
