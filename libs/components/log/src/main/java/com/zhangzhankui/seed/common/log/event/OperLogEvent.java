package com.zhangzhankui.seed.common.log.event;

import org.springframework.context.ApplicationEvent;

/** 操作日志事件 */
public class OperLogEvent extends ApplicationEvent {

  public OperLogEvent(OperLog operLog) {
    super(operLog);
  }

  public OperLog getOperLog() {
    return (OperLog) getSource();
  }
}
