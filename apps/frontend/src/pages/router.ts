/**
 * @file 路由配置
 */

import { createRouter, createWebHistory } from 'vue-router'
import { AccessTokenUtil } from '@/utils/token.util'
import TheLayout from '@/layout/TheLayout.vue'
import type { RouteRecordRaw, NavigationGuardNext, RouteLocationNormalized } from 'vue-router'

/**
 * 白名单路由（无需认证）
 */
const WHITE_LIST = new Set(['/login', '/401', '/404'])

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    component: TheLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: {
          requiresAuth: true,
          title: '仪表盘',
        },
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/users/UsersView.vue'),
        meta: {
          requiresAuth: true,
          title: '用户管理',
        },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: {
          requiresAuth: true,
          title: '个人中心',
        },
      },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: {
      title: '登录',
    },
  },
  {
    path: '/401',
    name: 'Unauthorized',
    component: () => import('@/pages/ErrorPage.vue'),
    meta: {
      title: '未授权',
      errorCode: 401,
    },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/pages/ErrorPage.vue'),
    meta: {
      title: '页面未找到',
      errorCode: 404,
    },
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

/**
 * 路由守卫
 */
router.beforeEach(
  async (
    to: RouteLocationNormalized,
    _from: RouteLocationNormalized,
    next: NavigationGuardNext
  ) => {
    // 设置页面标题
    const title = to.meta?.title as string | undefined
    document.title = title ? `${title} - Seed Cloud` : 'Seed Cloud'

    const isAuthenticated = AccessTokenUtil.exists()
    const requiresAuth = to.meta?.requiresAuth as boolean | undefined

    // 白名单路由直接放行
    if (WHITE_LIST.has(to.path)) {
      // 已登录用户访问登录页，跳转到首页
      if (to.path === '/login' && isAuthenticated) {
        const redirect = (to.query.redirect as string) || '/'
        next(redirect)
        return
      }
      next()
      return
    }

    // 需要认证但未登录（默认非白名单路由均需认证）
    if (requiresAuth !== false && !isAuthenticated) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }

    // Token 存在但用户信息未加载（页面刷新场景），先获取用户信息
    if (isAuthenticated) {
      try {
        const { useUserStore } = await import('@/stores/user/user.store')
        const userStore = useUserStore()
        if (!userStore.state.user) {
          const user = await userStore.fetchUserInfo()
          // 获取失败但 Token 存在，可能是网络问题（非 401）
          if (!user && AccessTokenUtil.exists()) {
            // 延迟导入避免循环依赖
            const { ElMessage } = await import('element-plus')
            ElMessage.warning('获取用户信息失败，部分功能可能受限')
          }
        }
      } catch (error) {
        // 401 由 HTTP 拦截器处理跳转登录，其他错误记录日志后放行
        console.warn('路由守卫获取用户信息失败:', error instanceof Error ? error.message : error)
      }
    }

    next()
  }
)

export default router
