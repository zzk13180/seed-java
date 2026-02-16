package com.zhangzhankui.seed.common.redis.cache;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.zhangzhankui.seed.common.core.cache.ICache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * ICache 接口的 Redis 实现
 *
 * <p>基于 Spring Data Redis 实现统一缓存接口，支持泛型操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCacheImpl implements ICache<String, Object> {

  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public Object get(@NonNull String key) {
    try {
      return redisTemplate.opsForValue().get(key);
    } catch (Exception e) {
      log.error("获取缓存失败, key: {}", key, e);
      return null;
    }
  }

  @Override
  public void put(@NonNull String key, Object value) {
    try {
      redisTemplate.opsForValue().set(key, value);
    } catch (Exception e) {
      log.error("设置缓存失败, key: {}", key, e);
    }
  }

  @Override
  public void put(@NonNull String key, Object value, long timeout, @NonNull TimeUnit timeUnit) {
    try {
      redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    } catch (Exception e) {
      log.error("设置缓存失败, key: {}, timeout: {}", key, timeout, e);
    }
  }

  @Override
  public boolean putIfAbsent(@NonNull String key, Object value) {
    try {
      return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value));
    } catch (Exception e) {
      log.error("设置缓存失败, key: {}", key, e);
      return false;
    }
  }

  @Override
  public boolean putIfAbsent(
      @NonNull String key, Object value, long timeout, @NonNull TimeUnit timeUnit) {
    try {
      return Boolean.TRUE.equals(
          redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit));
    } catch (Exception e) {
      log.error("设置缓存失败, key: {}", key, e);
      return false;
    }
  }

  @Override
  public boolean remove(@NonNull String key) {
    try {
      return Boolean.TRUE.equals(redisTemplate.delete(key));
    } catch (Exception e) {
      log.error("删除缓存失败, key: {}", key, e);
      return false;
    }
  }

  @Override
  public long remove(@NonNull Collection<String> keys) {
    try {
      Long deleted = redisTemplate.delete(keys);
      return deleted != null ? deleted : 0L;
    } catch (Exception e) {
      log.error("批量删除缓存失败, keys: {}", keys, e);
      return 0L;
    }
  }

  @Override
  public boolean containsKey(@NonNull String key) {
    try {
      return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    } catch (Exception e) {
      log.error("检查缓存存在失败, key: {}", key, e);
      return false;
    }
  }

  @Override
  public boolean expire(@NonNull String key, long timeout, @NonNull TimeUnit timeUnit) {
    try {
      return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, timeUnit));
    } catch (Exception e) {
      log.error("设置过期时间失败, key: {}", key, e);
      return false;
    }
  }

  @Override
  public long getExpire(@NonNull String key, @NonNull TimeUnit timeUnit) {
    try {
      Long expire = redisTemplate.getExpire(key, timeUnit);
      return expire != null ? expire : -2L;
    } catch (Exception e) {
      log.error("获取过期时间失败, key: {}", key, e);
      return -2L;
    }
  }

  @Override
  public void clear() {
    log.warn("清空所有缓存操作被调用，请确认这是预期行为");
    try {
      Set<String> keys = scanKeys("*");
      if (!keys.isEmpty()) {
        redisTemplate.delete(keys);
      }
    } catch (Exception e) {
      log.error("清空缓存失败", e);
    }
  }

  @Override
  public long size() {
    try {
      // 使用 DBSIZE 命令获取数据库大小，比 KEYS * 更高效
      Long size = redisTemplate.execute(RedisConnection::dbSize);
      return size != null ? size : 0L;
    } catch (Exception e) {
      log.error("获取缓存大小失败", e);
      return 0L;
    }
  }

  // ========== 扩展方法（Redis 特有）==========

  /**
   * 使用 SCAN 命令安全地扫描匹配的 key（不会阻塞 Redis）
   *
   * @param pattern 模式，如 "user:*"
   * @return 匹配的 key 集合
   */
  public Set<String> scanKeys(@NonNull String pattern) {
    Set<String> keys = new HashSet<>();
    try {
      ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
      redisTemplate.execute((RedisConnection connection) -> {
        try (Cursor<byte[]> cursor = connection.scan(options)) {
          while (cursor.hasNext()) {
            keys.add(new String(cursor.next()));
          }
        }
        return null;
      });
    } catch (Exception e) {
      log.error("扫描 key 失败, pattern: {}", pattern, e);
    }
    return keys;
  }

  /**
   * 按模式删除缓存（使用 SCAN 安全扫描）
   *
   * @param pattern 模式，如 "user:*"
   * @return 删除数量
   */
  public long removeByPattern(@NonNull String pattern) {
    try {
      Set<String> keys = scanKeys(pattern);
      if (!keys.isEmpty()) {
        Long deleted = redisTemplate.delete(keys);
        return deleted != null ? deleted : 0L;
      }
      return 0L;
    } catch (Exception e) {
      log.error("按模式删除缓存失败, pattern: {}", pattern, e);
      return 0L;
    }
  }

  /**
   * 递增
   *
   * @param key 键
   * @param delta 增量
   * @return 递增后的值
   */
  public long increment(@NonNull String key, long delta) {
    try {
      Long result = redisTemplate.opsForValue().increment(key, delta);
      return result != null ? result : 0L;
    } catch (Exception e) {
      log.error("递增操作失败, key: {}", key, e);
      return 0L;
    }
  }
}
