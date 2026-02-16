package com.zhangzhankui.seed.common.log.listener;

import com.zhangzhankui.seed.common.log.config.AsyncLogConfig;
import com.zhangzhankui.seed.common.log.event.OperLogEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 操作日志事件监听器
 *
 * <p>异步处理操作日志事件，将日志持久化到数据库或其他存储
 */
@Slf4j
@Component
public class OperLogListener {

  /**
   * 异步处理操作日志事件
   *
   * <p>使用 @Async 注解确保日志处理在独立线程中执行，不阻塞业务线程
   *
   * @param event 操作日志事件
   */
  @Async(AsyncLogConfig.ASYNC_LOG_EXECUTOR)
  @EventListener
  public void handleOperLog(OperLogEvent event) {
    try {
      // 当前仅打印日志，实际使用时应保存到数据库
      if (log.isDebugEnabled()) {
        log.debug(
            "操作日志: [{}] {} - {} (耗时: {}ms)",
            event.getOperLog().getBusinessType(),
            event.getOperLog().getTitle(),
            event.getOperLog().getMethod(),
            event.getOperLog().getCostTime());
      }
    } catch (Exception e) {
      log.error("保存操作日志失败", e);
    }
  }
}
