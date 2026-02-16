/**
 * @file 错误处理
 *
 * Why: 纯函数式设计，替代原有的 interface + class + factory + singleton 四层抽象。
 * 一个 parseError() 函数 + 一张映射表即可完成同样工作。
 */

import { HttpError as HttpClientError } from './http.client'

// ─── 类型 ────────────────────────────────────────────

export enum ErrorType {
  NETWORK_ERROR = 'NETWORK_ERROR',
  TIMEOUT_ERROR = 'TIMEOUT_ERROR',
  SERVICE_UNAVAILABLE = 'SERVICE_UNAVAILABLE',
  UNAUTHORIZED = 'UNAUTHORIZED',
  FORBIDDEN = 'FORBIDDEN',
  NOT_FOUND = 'NOT_FOUND',
  CLIENT_ERROR = 'CLIENT_ERROR',
  SERVER_ERROR = 'SERVER_ERROR',
  UNKNOWN_ERROR = 'UNKNOWN_ERROR',
}

export interface ErrorInfo {
  type: ErrorType
  title: string
  message: string
  status: number
  requiresReLogin: boolean
}

// ─── 消息映射 ────────────────────────────────────────

const ERROR_MESSAGES: Record<ErrorType, { title: string; message: string }> = {
  [ErrorType.NETWORK_ERROR]: { title: '网络连接失败', message: '无法连接到服务器，请检查网络连接' },
  [ErrorType.TIMEOUT_ERROR]: { title: '请求超时', message: '服务器响应超时，请稍后重试' },
  [ErrorType.SERVICE_UNAVAILABLE]: { title: '服务暂不可用', message: '服务器正在维护中，请稍后重试' },
  [ErrorType.UNAUTHORIZED]: { title: '登录已过期', message: '您的登录状态已过期，请重新登录' },
  [ErrorType.FORBIDDEN]: { title: '无权访问', message: '您没有权限执行此操作' },
  [ErrorType.NOT_FOUND]: { title: '资源不存在', message: '请求的资源不存在或已被删除' },
  [ErrorType.CLIENT_ERROR]: { title: '请求错误', message: '请求参数错误，请检查后重试' },
  [ErrorType.SERVER_ERROR]: { title: '服务器错误', message: '服务器内部错误，请稍后重试' },
  [ErrorType.UNKNOWN_ERROR]: { title: '未知错误', message: '发生未知错误，请稍后重试' },
}

// ─── 核心逻辑 ────────────────────────────────────────

function classifyStatus(status: number, message?: string): ErrorType {
  if (status === 0) {
    return message?.toLowerCase().includes('timeout')
      ? ErrorType.TIMEOUT_ERROR
      : ErrorType.NETWORK_ERROR
  }
  if (status === 401) {
    return ErrorType.UNAUTHORIZED
  }
  if (status === 403) {
    return ErrorType.FORBIDDEN
  }
  if (status === 404) {
    return ErrorType.NOT_FOUND
  }
  if (status >= 400 && status < 500) {
    return ErrorType.CLIENT_ERROR
  }
  if (status === 502 || status === 503 || status === 504) {
    return ErrorType.SERVICE_UNAVAILABLE
  }
  if (status >= 500) {
    return ErrorType.SERVER_ERROR
  }
  return ErrorType.UNKNOWN_ERROR
}

function classifyError(error: unknown): ErrorType {
  if (error instanceof HttpClientError) {
    return classifyStatus(error.status, error.message)
  }
  if (error instanceof Error) {
    const msg = error.message.toLowerCase()
    if (msg.includes('network') || msg.includes('failed to fetch') || error.name === 'TypeError') {
      return ErrorType.NETWORK_ERROR
    }
    if (msg.includes('timeout') || error.name === 'AbortError') {
      return ErrorType.TIMEOUT_ERROR
    }
  }
  return ErrorType.UNKNOWN_ERROR
}

/** 尝试从后端响应体中提取人类可读的错误信息 */
function extractServerMessage(error: unknown): string | null {
  if (!(error instanceof HttpClientError) || !error.data) {
    return null
  }
  const data = error.data as Record<string, unknown>
  for (const key of ['message', 'msg', 'error', 'detail']) {
    if (typeof data[key] === 'string') {
      return data[key] as string
    }
  }
  return null
}

/**
 * 解析任意错误为用户友好的 ErrorInfo
 */
export function parseError(error: unknown): ErrorInfo {
  const type = classifyError(error)
  const defaults = ERROR_MESSAGES[type]
  const status = error instanceof HttpClientError ? error.status : 0
  return {
    type,
    title: defaults.title,
    message: extractServerMessage(error) || defaults.message,
    status,
    requiresReLogin: type === ErrorType.UNAUTHORIZED,
  }
}
