/**
 * @file 日志服务
 * @description 提供统一的日志接口，支持模块化日志记录
 */

export type LogLevel = 'debug' | 'info' | 'warn' | 'error'

export interface Logger {
  debug(message: string, data?: unknown): void
  info(message: string, data?: unknown): void
  warn(message: string, data?: unknown): void
  error(message: string, error?: unknown): void
}

/**
 * 日志级别优先级
 */
const LOG_LEVEL_PRIORITY: Record<LogLevel, number> = {
  debug: 0,
  info: 1,
  warn: 2,
  error: 3,
}

/**
 * 当前日志级别
 * - 开发环境：显示所有日志 (debug)
 * - 生产环境：仅显示错误 (error)，避免敏感信息泄露
 */
const currentLevel: LogLevel = import.meta.env.DEV ? 'debug' : 'error'

/**
 * 判断是否应该输出该级别的日志
 */
function shouldLog(level: LogLevel): boolean {
  return LOG_LEVEL_PRIORITY[level] >= LOG_LEVEL_PRIORITY[currentLevel]
}

/**
 * 格式化日志消息
 */
function formatMessage(module: string, message: string): string {
  const timestamp = new Date().toISOString()
  return `[${timestamp}] [${module}] ${message}`
}

/**
 * 创建模块化日志记录器
 * @param module - 模块名称
 */
export function createLogger(module: string): Logger {
  return {
    debug(message: string, data?: unknown): void {
      if (shouldLog('debug')) {
        if (data !== undefined) {
          console.debug(formatMessage(module, message), data)
        } else {
          console.debug(formatMessage(module, message))
        }
      }
    },

    info(message: string, data?: unknown): void {
      if (shouldLog('info')) {
        if (data !== undefined) {
          console.info(formatMessage(module, message), data)
        } else {
          console.info(formatMessage(module, message))
        }
      }
    },

    warn(message: string, data?: unknown): void {
      if (shouldLog('warn')) {
        if (data !== undefined) {
          console.warn(formatMessage(module, message), data)
        } else {
          console.warn(formatMessage(module, message))
        }
      }
    },

    error(message: string, error?: unknown): void {
      if (shouldLog('error')) {
        if (error !== undefined) {
          console.error(formatMessage(module, message), error)
        } else {
          console.error(formatMessage(module, message))
        }
      }
    },
  }
}
