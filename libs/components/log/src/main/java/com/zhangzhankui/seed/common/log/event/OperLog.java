package com.zhangzhankui.seed.common.log.event;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/** 操作日志 */
@Data
public class OperLog implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 日志ID */
  private Long operId;

  /** 租户ID */
  private String tenantId;

  /** 模块标题 */
  private String title;

  /** 业务类型 */
  private Integer businessType;

  /** 请求方法 */
  private String method;

  /** 请求方式 */
  private String requestMethod;

  /** 操作人类别 */
  private Integer operatorType;

  /** 操作人员 */
  private String operName;

  /** 部门名称 */
  private String deptName;

  /** 请求URL */
  private String operUrl;

  /** 操作地址 */
  private String operIp;

  /** 操作地点 */
  private String operLocation;

  /** 请求参数 */
  private String operParam;

  /** 返回参数 */
  private String jsonResult;

  /** 操作状态 (0正常 1异常) */
  private Integer status;

  /** 错误消息 */
  private String errorMsg;

  /** 操作时间 */
  private LocalDateTime operTime;

  /** 消耗时间 */
  private Long costTime;
}
