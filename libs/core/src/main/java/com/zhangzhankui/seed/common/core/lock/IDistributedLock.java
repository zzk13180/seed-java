package com.zhangzhankui.seed.common.core.lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁接口
 *
 * <p>提供统一的分布式锁操作规范，支持不同的实现（Redisson、Zookeeper等）
 */
public interface IDistributedLock {

  /**
   * 尝试获取锁
   *
   * @param lockKey 锁键
   * @param waitTime 等待时间
   * @param leaseTime 锁持有时间（过期自动释放）
   * @param unit 时间单位
   * @return 是否获取成功
   */
  boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit);

  /**
   * 尝试获取锁（使用默认等待时间）
   *
   * @param lockKey 锁键
   * @param leaseTime 锁持有时间
   * @param unit 时间单位
   * @return 是否获取成功
   */
  default boolean tryLock(String lockKey, long leaseTime, TimeUnit unit) {
    return tryLock(lockKey, 0, leaseTime, unit);
  }

  /**
   * 获取锁（阻塞等待）
   *
   * @param lockKey 锁键
   * @param leaseTime 锁持有时间
   * @param unit 时间单位
   */
  void lock(String lockKey, long leaseTime, TimeUnit unit);

  /**
   * 获取锁（阻塞等待，使用默认过期时间）
   *
   * @param lockKey 锁键
   */
  default void lock(String lockKey) {
    lock(lockKey, 30, TimeUnit.SECONDS);
  }

  /**
   * 释放锁
   *
   * @param lockKey 锁键
   */
  void unlock(String lockKey);

  /**
   * 判断是否持有锁
   *
   * @param lockKey 锁键
   * @return 是否持有
   */
  boolean isLocked(String lockKey);

  /**
   * 判断当前线程是否持有锁
   *
   * @param lockKey 锁键
   * @return 是否持有
   */
  boolean isHeldByCurrentThread(String lockKey);

  /**
   * 在锁保护下执行操作（无返回值）
   *
   * @param lockKey 锁键
   * @param leaseTime 锁持有时间
   * @param unit 时间单位
   * @param runnable 要执行的操作
   * @return 是否执行成功（获取到锁并执行）
   */
  default boolean executeWithLock(
      String lockKey, long leaseTime, TimeUnit unit, Runnable runnable) {
    if (tryLock(lockKey, 0, leaseTime, unit)) {
      try {
        runnable.run();
        return true;
      } finally {
        unlock(lockKey);
      }
    }
    return false;
  }

  /**
   * 在锁保护下执行操作（有返回值）
   *
   * @param lockKey 锁键
   * @param leaseTime 锁持有时间
   * @param unit 时间单位
   * @param supplier 要执行的操作
   * @return 执行结果，获取锁失败返回 null
   */
  default <T> T executeWithLock(
      String lockKey, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
    if (tryLock(lockKey, 0, leaseTime, unit)) {
      try {
        return supplier.get();
      } finally {
        unlock(lockKey);
      }
    }
    return null;
  }

  /**
   * 在锁保护下执行操作（等待获取锁）
   *
   * @param lockKey 锁键
   * @param waitTime 等待时间
   * @param leaseTime 锁持有时间
   * @param unit 时间单位
   * @param supplier 要执行的操作
   * @return 执行结果，获取锁失败返回 null
   */
  default <T> T executeWithLock(
      String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
    if (tryLock(lockKey, waitTime, leaseTime, unit)) {
      try {
        return supplier.get();
      } finally {
        unlock(lockKey);
      }
    }
    return null;
  }
}
