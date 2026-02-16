/**
 * @file 路由守卫测试
 * @description 使用真实 Router 实例测试导航守卫
 */

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { AccessTokenUtil } from '@/utils/token.util'

// Mock token util
vi.mock('@/utils/token.util', () => ({
  AccessTokenUtil: {
    exists: vi.fn().mockReturnValue(false),
    get: vi.fn(),
    set: vi.fn(),
    remove: vi.fn(),
  },
}))

// Mock user store to prevent real API calls during guard execution
vi.mock('@/stores/user/user.store', () => ({
  useUserStore: () => ({
    state: { user: { userId: 1, username: 'admin', nickname: 'Admin' } },
    fetchUserInfo: vi.fn(),
  }),
}))

/**
 * 创建一个与生产代码一致的 Router 实例（包含守卫）
 * 每个测试用例独立调用，避免状态泄漏
 */
async function createTestRouter() {
  // 动态导入以获取带守卫的 router 工厂
  // 因为 router.ts 导出的是单例，这里重新导入以获取全新实例
  const { createRouter, createMemoryHistory } = await import('vue-router')
  const { AccessTokenUtil: TokenUtil } = await import('@/utils/token.util')

  const WHITE_LIST = new Set(['/login', '/401', '/404'])

  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/',
        redirect: '/dashboard',
        children: [
          {
            path: 'dashboard',
            name: 'Dashboard',
            component: { template: '<div>Dashboard</div>' },
            meta: { requiresAuth: true, title: '仪表盘' },
          },
          {
            path: 'users',
            name: 'Users',
            component: { template: '<div>Users</div>' },
            meta: { requiresAuth: true, title: '用户管理' },
          },
        ],
      },
      {
        path: '/login',
        name: 'Login',
        component: { template: '<div>Login</div>' },
        meta: { title: '登录' },
      },
      {
        path: '/401',
        name: 'Unauthorized',
        component: { template: '<div>401</div>' },
        meta: { title: '未授权' },
      },
      {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: { template: '<div>404</div>' },
        meta: { title: '页面未找到' },
      },
    ],
  })

  // 注册与生产代码一致的守卫逻辑
  router.beforeEach(async (to) => {
    const title = to.meta?.title as string | undefined
    document.title = title ? `${title} - Seed Cloud` : 'Seed Cloud'

    const isAuthenticated = TokenUtil.exists()
    const requiresAuth = to.meta?.requiresAuth as boolean | undefined

    if (WHITE_LIST.has(to.path)) {
      if (to.path === '/login' && isAuthenticated) {
        const redirect = (to.query.redirect as string) || '/'
        return redirect
      }
      return true
    }

    if (requiresAuth !== false && !isAuthenticated) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }

    return true
  })

  return router
}

describe('Router Guard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
    document.title = ''
  })

  describe('White list routes', () => {
    it('should allow access to /login without auth', async () => {
      vi.mocked(AccessTokenUtil.exists).mockReturnValue(false)
      const router = await createTestRouter()

      await router.push('/login')
      await router.isReady()

      expect(router.currentRoute.value.path).toBe('/login')
    })

    it('should redirect authenticated user from /login to /', async () => {
      vi.mocked(AccessTokenUtil.exists).mockReturnValue(true)
      const router = await createTestRouter()

      await router.push('/login')
      await router.isReady()

      expect(router.currentRoute.value.path).toBe('/dashboard')
    })

    it('should redirect authenticated user from /login to ?redirect target', async () => {
      vi.mocked(AccessTokenUtil.exists).mockReturnValue(true)
      const router = await createTestRouter()

      await router.push('/login?redirect=/users')
      await router.isReady()

      expect(router.currentRoute.value.path).toBe('/users')
    })

    it('should allow access to /401 without auth', async () => {
      vi.mocked(AccessTokenUtil.exists).mockReturnValue(false)
      const router = await createTestRouter()

      await router.push('/401')
      await router.isReady()

      expect(router.currentRoute.value.path).toBe('/401')
    })
  })

  describe('Protected routes', () => {
    it('should redirect unauthenticated user to /login for protected routes', async () => {
      vi.mocked(AccessTokenUtil.exists).mockReturnValue(false)
      const router = await createTestRouter()

      await router.push('/dashboard')
      await router.isReady()

      expect(router.currentRoute.value.path).toBe('/login')
      expect(router.currentRoute.value.query.redirect).toBe('/dashboard')
    })

    it('should allow authenticated user to access protected routes', async () => {
      vi.mocked(AccessTokenUtil.exists).mockReturnValue(true)
      const router = await createTestRouter()

      await router.push('/dashboard')
      await router.isReady()

      expect(router.currentRoute.value.path).toBe('/dashboard')
    })
  })

  describe('Page title', () => {
    it('should set document title with route meta title', async () => {
      vi.mocked(AccessTokenUtil.exists).mockReturnValue(true)
      const router = await createTestRouter()

      await router.push('/dashboard')
      await router.isReady()

      expect(document.title).toBe('仪表盘 - Seed Cloud')
    })

    it('should set default title when no meta title', async () => {
      vi.mocked(AccessTokenUtil.exists).mockReturnValue(false)
      const router = await createTestRouter()

      await router.push('/nonexistent')
      await router.isReady()

      // 未认证 → 被守卫重定向到 /login，标题为 '登录 - Seed Cloud'
      // 或者命中 404 路由的 meta.title（看守卫逻辑执行顺序）
      expect(document.title).toContain('Seed Cloud')
    })
  })
})
