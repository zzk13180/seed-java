/**
 * @file 网络状态监听服务
 * @description 监听网络状态变化，提供在线/离线通知
 */

import { ElMessage } from 'element-plus'
import { createLogger } from './logger.service'

const logger = createLogger('Network')

let isOnline = navigator.onLine
let hasShownOfflineMessage = false

/**
 * 处理网络上线事件
 */
function handleOnline(): void {
  logger.info('Network online')
  isOnline = true

  if (hasShownOfflineMessage) {
    ElMessage.success('网络已恢复连接')
    hasShownOfflineMessage = false
  }
}

/**
 * 处理网络离线事件
 */
function handleOffline(): void {
  logger.warn('Network offline')
  isOnline = false
  hasShownOfflineMessage = true

  ElMessage.warning({
    message: '网络连接已断开，请检查网络设置',
    duration: 5000,
  })
}

/**
 * 设置网络状态监听
 */
export function setupNetworkListener(): void {
  window.addEventListener('online', handleOnline)
  window.addEventListener('offline', handleOffline)

  logger.info('Network listener initialized', { isOnline: navigator.onLine })
}

/**
 * 获取当前网络状态
 */
export function isNetworkOnline(): boolean {
  return isOnline
}

/**
 * 移除网络状态监听
 */
export function removeNetworkListener(): void {
  window.removeEventListener('online', handleOnline)
  window.removeEventListener('offline', handleOffline)
}
