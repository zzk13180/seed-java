/**
 * @file HTTP 拦截器模块测试
 * @description 测试 setupHttpInterceptors 函数和 http 客户端实例
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

// Mock element-plus
vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn(),
    success: vi.fn(),
    warning: vi.fn(),
  },
  ElNotification: vi.fn(),
}))

// Mock token util
vi.mock('@/utils/token.util', () => ({
  AccessTokenUtil: {
    get: vi.fn(),
    set: vi.fn(),
    remove: vi.fn(),
  },
}))

// Mock logger service
vi.mock('@/core/logger.service', () => ({
  createLogger: vi.fn(() => ({
    info: vi.fn(),
    debug: vi.fn(),
    warn: vi.fn(),
    error: vi.fn(),
  })),
}))

// Mock user store
vi.mock('@/stores/user/user.store', () => ({
  useUserStore: vi.fn(() => ({
    state: { user: null },
  })),
}))

describe('HTTP Interceptor Module', () => {
  beforeEach(() => {
    vi.resetModules()
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  describe('http instance', () => {
    it('should export a configured HttpClient instance', async () => {
      const { $http } = await import('../http.interceptor')

      expect($http).toBeDefined()
      expect(typeof $http.get).toBe('function')
      expect(typeof $http.post).toBe('function')
      expect(typeof $http.put).toBe('function')
      expect(typeof $http.delete).toBe('function')
    })

    it('should have default timeout of 30 seconds', async () => {
      const { $http } = await import('../http.interceptor')

      expect($http.timeout).toBe(30000)
    })
  })

  describe('setupHttpInterceptors', () => {
    it('should be idempotent - multiple calls should not add duplicate interceptors', async () => {
      const { setupHttpInterceptors, $http } = await import('../http.interceptor')

      const initialRequestInterceptorsCount = $http['requestInterceptors'].length
      const initialErrorInterceptorsCount = $http['errorInterceptors'].length

      // Call setup twice
      setupHttpInterceptors()
      setupHttpInterceptors()

      // Should only add interceptors once
      expect($http['requestInterceptors'].length).toBe(initialRequestInterceptorsCount + 1)
      expect($http['errorInterceptors'].length).toBe(initialErrorInterceptorsCount + 1)
    })
  })

  describe('Request interceptor behavior', () => {
    // HttpOnly Cookie 模式：认证依赖 credentials: 'include' 自动携带 Cookie，
    // 请求拦截器不设置 Authorization 头
    it('should not inject Authorization header (HttpOnly Cookie mode)', async () => {
      vi.resetModules()
      const { AccessTokenUtil } = await import('@/utils/token.util')
      vi.mocked(AccessTokenUtil.get).mockReturnValue('test-token')

      const { setupHttpInterceptors, $http } = await import('../http.interceptor')
      setupHttpInterceptors()

      // Get the request interceptor
      const requestInterceptors = $http['requestInterceptors']
      expect(requestInterceptors.length).toBeGreaterThan(0)

      // Test the interceptor - should pass through config unchanged
      const interceptor = requestInterceptors[requestInterceptors.length - 1]
      const config = {
        endpoint: '/test',
        method: 'GET',
        headers: {},
      }

      const result = await Promise.resolve(interceptor(config))
      // HttpOnly Cookie mode: no Authorization header injection
      expect(result.headers?.Authorization).toBeUndefined()
      expect(result.endpoint).toBe('/test')
    })

    it('should pass through config unchanged when no token', async () => {
      vi.resetModules()
      const { AccessTokenUtil } = await import('@/utils/token.util')
      vi.mocked(AccessTokenUtil.get).mockReturnValue(null)

      const { setupHttpInterceptors, $http } = await import('../http.interceptor')
      setupHttpInterceptors()

      const requestInterceptors = $http['requestInterceptors']
      const interceptor = requestInterceptors[requestInterceptors.length - 1]
      const config = {
        endpoint: '/test',
        method: 'GET',
        headers: {},
      }

      const result = await Promise.resolve(interceptor(config))
      expect(result.headers?.Authorization).toBeUndefined()
    })
  })
})
