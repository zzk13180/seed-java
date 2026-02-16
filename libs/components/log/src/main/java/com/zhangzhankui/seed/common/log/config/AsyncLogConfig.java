package com.zhangzhankui.seed.common.log.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 日志异步配置
 *
 * <p>使用 Java 21 虚拟线程执行异步日志写入，避免阻塞业务线程
 */
@EnableAsync
@Configuration
public class AsyncLogConfig {

  /** 日志异步执行器 Bean 名称 */
  public static final String ASYNC_LOG_EXECUTOR = "asyncLogExecutor";

  /**
   * 异步日志执行器
   *
   * <p>使用 Java 21 虚拟线程执行器，具有以下优势：
   *
   * <ul>
   *   <li>轻量级线程，无需维护线程池大小
   *   <li>每个任务一个虚拟线程，无队列限制
   *   <li>自动利用平台线程，高效执行 I/O 操作
   * </ul>
   */
  @Bean(ASYNC_LOG_EXECUTOR)
  public Executor asyncLogExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
