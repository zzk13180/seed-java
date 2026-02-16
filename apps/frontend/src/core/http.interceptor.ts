/**
 * @file HTTP 拦截器配置
 * @description 配置全局请求/响应/错误拦截器，提供统一的错误处理、用户提示和 Token 续期
 */

import { ElMessage, ElNotification } from 'element-plus'
import { AccessTokenUtil } from '@/utils/token.util'
import { HttpClient } from './http.client'
import { parseError, ErrorType, type ErrorInfo } from './error.service'
import { createLogger } from './logger.service'

const logger = createLogger('HTTP')

/**
 * 记录最后一次错误通知的时间戳，用于防止重复提示
 */
let lastErrorNotificationTime = 0
const ERROR_NOTIFICATION_COOLDOWN = 3000

let lastErrorType: ErrorType | null = null

/**
 * 创建 HttpClient 实例
 * 默认不自动重试，业务代码可根据需求在调用时指定 retries
 */
export const $http = new HttpClient(import.meta.env.VITE_API_BASE_PATH || '/api', {
  timeout: 30_000,
  retries: 0,
  retryDelay: 1000,
})

/**
 * 显示错误通知给用户
 */
function showErrorNotification(errorInfo: ErrorInfo): void {
  const now = Date.now()

  if (
    lastErrorType === errorInfo.type &&
    now - lastErrorNotificationTime < ERROR_NOTIFICATION_COOLDOWN
  ) {
    logger.debug('Skipping duplicate error notification', { type: errorInfo.type })
    return
  }

  lastErrorNotificationTime = now
  lastErrorType = errorInfo.type

  switch (errorInfo.type) {
    case ErrorType.NETWORK_ERROR:
    case ErrorType.SERVICE_UNAVAILABLE:
    case ErrorType.TIMEOUT_ERROR: {
      ElNotification({
        title: errorInfo.title,
        message: errorInfo.message,
        type: 'error',
        duration: 5000,
        position: 'top-right',
      })
      break
    }

    case ErrorType.UNAUTHORIZED: {
      ElNotification({
        title: errorInfo.title,
        message: errorInfo.message,
        type: 'warning',
        duration: 4000,
        position: 'top-right',
      })
      break
    }

    case ErrorType.FORBIDDEN:
    case ErrorType.NOT_FOUND:
    case ErrorType.CLIENT_ERROR: {
      ElMessage.error(errorInfo.message)
      break
    }

    case ErrorType.SERVER_ERROR: {
      ElNotification({
        title: errorInfo.title,
        message: errorInfo.message,
        type: 'error',
        duration: 4000,
        position: 'top-right',
      })
      break
    }

    default: {
      ElMessage.error(errorInfo.message)
    }
  }
}

/**
 * 是否正在刷新 Token
 */
let isRefreshing = false
let failedQueue: Array<{
  resolve: (value?: unknown) => void
  reject: (reason?: unknown) => void
}> = []

function processQueue(error: unknown, token: string | null = null): void {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error)
    } else {
      resolve(token)
    }
  })
  failedQueue = []
}

/**
 * 处理认证错误：清除状态并导航到登录页
 */
async function handleAuthError(): Promise<void> {
  AccessTokenUtil.remove()
  const { useUserStore } = await import('@/stores/user/user.store')
  try {
    const userStore = useUserStore()
    await userStore.logout()
  } catch {
    // store 未初始化时静默
  }
  // 延迟导航，给通知显示时间
  const { router } = await import('@/pages/router')
  setTimeout(() => {
    router.push('/login')
  }, 1500)
}

/**
 * 设置 HTTP 拦截器（幂等，仅初始化一次）
 */
let interceptorsInitialized = false

export function setupHttpInterceptors(): void {
  if (interceptorsInitialized) {
    return
  }
  interceptorsInitialized = true

  // 请求拦截器：HttpOnly Cookie 模式下不设置 Authorization 头
  // 认证完全依赖 credentials: 'include' 自动携带 Cookie
  $http.addRequestInterceptor((config) => {
    logger.debug('Request', { endpoint: config.endpoint, method: config.method })
    return config
  })

  // 响应拦截器：检查业务状态码
  $http.addResponseInterceptor((response) => {
    // 检查后端返回的业务状态码（HTTP 200 但业务失败的情况）
    const data = response as Record<string, unknown>
    if (data && typeof data.code === 'number' && data.code !== 200) {
      const message = (data.message as string) || '请求失败'
      ElMessage.error(message)
      throw new Error(message)
    }
    return response
  })

  // 错误拦截器：处理 401 Token 续期和错误通知
  $http.addErrorInterceptor(async (error, config) => {
    // refresh 端点本身返回 401 — 直接跳转登录，避免死循环
    if (error.status === 401 && config.endpoint.includes('/auth/refresh')) {
      logger.warn('Token refresh failed with 401, redirecting to login')
      const errorInfo = parseError(error)
      showErrorNotification(errorInfo)
      await handleAuthError()
      throw error
    }

    // 401 未授权 - 尝试透明 Token 续期
    // 跳过 refresh 端点本身，避免死循环
    if (error.status === 401 && !config.endpoint.includes('/auth/refresh')) {
      if (isRefreshing) {
        // 如果正在刷新 token，将请求加入队列等待
        return new Promise((resolve, reject) => {
          failedQueue.push({
            resolve: async () => {
              try {
                // HttpOnly Cookie 模式下无需设置 Authorization header
                const result = await $http.request(
                  config.endpoint,
                  config.method,
                  config.params,
                  config.headers,
                )
                resolve(result)
              } catch (retryError) {
                reject(retryError)
              }
            },
            reject,
          })
        })
      }

      isRefreshing = true

      try {
        // 直接调用 refresh 端点
        // HttpOnly Cookie 模式下，刷新 Token 由服务端通过 Set-Cookie 完成
        await $http.post<{ code: number; data: { token: string } }>('/auth/refresh')
        processQueue(null, null)

        // 用新 token 重试原请求（HttpOnly Cookie 模式下无需设置 header）
        return await $http.request(
          config.endpoint,
          config.method,
          config.params,
          config.headers,
        )
      } catch (refreshError) {
        processQueue(refreshError, null)
        // Token 续期失败，跳转到登录页
        const errorInfo = parseError(error)
        showErrorNotification(errorInfo)
        await handleAuthError()
        throw error
      } finally {
        isRefreshing = false
      }
    }

    // 非 401 错误：直接解析错误
    const errorInfo = parseError(error)

    showErrorNotification(errorInfo)

    throw error
  })

  logger.info('HTTP interceptors initialized')
}
