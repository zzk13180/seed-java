package com.zhangzhankui.seed.common.redis.lock;

import java.util.concurrent.TimeUnit;

import com.zhangzhankui.seed.common.core.lock.IDistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * 基于 Redisson 的分布式锁实现
 *
 * <p>特点：
 *
 * <ul>
 *   <li>可重入锁
 *   <li>自动续期（看门狗机制）
 *   <li>RedLock 算法支持
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonDistributedLock implements IDistributedLock {

  private static final String LOCK_PREFIX = "lock:";

  private final RedissonClient redissonClient;

  @Override
  public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
    String key = buildLockKey(lockKey);
    RLock lock = redissonClient.getLock(key);
    try {
      boolean acquired = lock.tryLock(waitTime, leaseTime, unit);
      if (acquired) {
        log.debug("成功获取分布式锁: {}", key);
      } else {
        log.debug("获取分布式锁失败: {}", key);
      }
      return acquired;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("获取分布式锁被中断: {}", key, e);
      return false;
    }
  }

  @Override
  public void lock(String lockKey, long leaseTime, TimeUnit unit) {
    String key = buildLockKey(lockKey);
    RLock lock = redissonClient.getLock(key);
    lock.lock(leaseTime, unit);
    log.debug("成功获取分布式锁（阻塞）: {}", key);
  }

  @Override
  public void unlock(String lockKey) {
    String key = buildLockKey(lockKey);
    RLock lock = redissonClient.getLock(key);
    if (lock.isHeldByCurrentThread()) {
      lock.unlock();
      log.debug("释放分布式锁: {}", key);
    } else {
      log.warn("尝试释放非当前线程持有的锁: {}", key);
    }
  }

  @Override
  public boolean isLocked(String lockKey) {
    String key = buildLockKey(lockKey);
    RLock lock = redissonClient.getLock(key);
    return lock.isLocked();
  }

  @Override
  public boolean isHeldByCurrentThread(String lockKey) {
    String key = buildLockKey(lockKey);
    RLock lock = redissonClient.getLock(key);
    return lock.isHeldByCurrentThread();
  }

  /**
   * 构建锁键
   *
   * @param lockKey 业务锁键
   * @return 完整锁键
   */
  private String buildLockKey(String lockKey) {
    return LOCK_PREFIX + lockKey;
  }

  // ========== 扩展方法（Redisson 特有）==========

  /**
   * 获取公平锁
   *
   * <p>按照请求顺序分配锁
   *
   * @param lockKey 锁键
   * @param waitTime 等待时间
   * @param leaseTime 锁持有时间
   * @param unit 时间单位
   * @return 是否获取成功
   */
  public boolean tryFairLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
    String key = buildLockKey(lockKey);
    RLock lock = redissonClient.getFairLock(key);
    try {
      boolean acquired = lock.tryLock(waitTime, leaseTime, unit);
      if (acquired) {
        log.debug("成功获取公平锁: {}", key);
      }
      return acquired;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("获取公平锁被中断: {}", key, e);
      return false;
    }
  }

  /**
   * 释放公平锁
   *
   * @param lockKey 锁键
   */
  public void unlockFairLock(String lockKey) {
    String key = buildLockKey(lockKey);
    RLock lock = redissonClient.getFairLock(key);
    if (lock.isHeldByCurrentThread()) {
      lock.unlock();
      log.debug("释放公平锁: {}", key);
    }
  }

  /**
   * 获取读锁
   *
   * @param lockKey 锁键
   * @param waitTime 等待时间
   * @param leaseTime 锁持有时间
   * @param unit 时间单位
   * @return 是否获取成功
   */
  public boolean tryReadLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
    String key = buildLockKey(lockKey);
    RLock readLock = redissonClient.getReadWriteLock(key).readLock();
    try {
      return readLock.tryLock(waitTime, leaseTime, unit);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  /**
   * 获取写锁
   *
   * @param lockKey 锁键
   * @param waitTime 等待时间
   * @param leaseTime 锁持有时间
   * @param unit 时间单位
   * @return 是否获取成功
   */
  public boolean tryWriteLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
    String key = buildLockKey(lockKey);
    RLock writeLock = redissonClient.getReadWriteLock(key).writeLock();
    try {
      return writeLock.tryLock(waitTime, leaseTime, unit);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  /**
   * 释放读锁
   *
   * @param lockKey 锁键
   */
  public void unlockReadLock(String lockKey) {
    String key = buildLockKey(lockKey);
    RLock readLock = redissonClient.getReadWriteLock(key).readLock();
    if (readLock.isHeldByCurrentThread()) {
      readLock.unlock();
    }
  }

  /**
   * 释放写锁
   *
   * @param lockKey 锁键
   */
  public void unlockWriteLock(String lockKey) {
    String key = buildLockKey(lockKey);
    RLock writeLock = redissonClient.getReadWriteLock(key).writeLock();
    if (writeLock.isHeldByCurrentThread()) {
      writeLock.unlock();
    }
  }
}
