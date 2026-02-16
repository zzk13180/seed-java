/**
 * @file Token 工具
 * @description 管理访问令牌的存储和获取
 *
 * 安全说明：
 * - Token 使用 HttpOnly Cookie 存储，前端无法直接访问
 * - 使用内存标记跟踪登录状态，实际认证由后端 Cookie 校验
 * - 用户信息使用 sessionStorage 存储
 */

const USER_KEY = 'seed_cloud_user'

/**
 * 内存中的认证状态标记
 * 用于前端 UI 状态判断，实际认证由后端 HttpOnly Cookie 校验
 */
let isAuthenticatedFlag = false

/**
 * 访问令牌工具类
 * HttpOnly Cookie 模式下，前端无法直接读取 Token
 * 使用内存标记跟踪登录状态
 */
export const AccessTokenUtil = {
  /**
   * 获取访问令牌（兼容旧接口）
   * HttpOnly Cookie 模式下返回 null，实际认证由 Cookie 自动携带
   */
  get(): string | null {
    return isAuthenticatedFlag ? 'httponly-cookie' : null
  },

  /**
   * 设置认证状态（登录成功后调用）
   * HttpOnly Cookie 由后端设置，前端只需标记状态
   */
  set(_token: string): void {
    isAuthenticatedFlag = true
  },

  /**
   * 移除认证状态（登出后调用）
   * HttpOnly Cookie 由后端清除，前端只需清除标记
   */
  remove(): void {
    isAuthenticatedFlag = false
  },

  /**
   * 检查是否存在认证状态
   */
  exists(): boolean {
    return isAuthenticatedFlag
  },

  /**
   * 从 sessionStorage 恢复认证状态（页面刷新时）
   * 如果有用户信息缓存，认为可能已登录，让后续请求验证
   */
  restoreFromUserCache(): void {
    const user = sessionStorage.getItem(USER_KEY)
    if (user) {
      isAuthenticatedFlag = true
    }
  },
}

/**
 * 用户信息存储工具
 * 使用 sessionStorage 存储，关闭标签页即清除
 */
export const UserStorageUtil = {
  /**
   * 获取用户信息
   */
  get<T = unknown>(): T | null {
    const data = sessionStorage.getItem(USER_KEY)
    if (!data) {
      return null
    }
    try {
      return JSON.parse(data) as T
    } catch {
      return null
    }
  },

  /**
   * 设置用户信息
   */
  set(user: unknown): void {
    sessionStorage.setItem(USER_KEY, JSON.stringify(user))
  },

  /**
   * 移除用户信息
   */
  remove(): void {
    sessionStorage.removeItem(USER_KEY)
  },
}

/**
 * 清除所有认证信息
 */
export function clearAuth(): void {
  AccessTokenUtil.remove()
  UserStorageUtil.remove()
}
