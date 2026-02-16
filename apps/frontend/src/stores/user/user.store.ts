/**
 * @file 用户状态管理
 *
 * Why: 合并了原 controller/service/types 三层抽象。
 * 在 Vue + Pinia 生态中，vi.mock() 已可替代接口注入实现测试隔离，
 * 无需 Java 风格 DI。store action 直接调用 API，逻辑一目了然。
 */

import { reactive, computed } from 'vue'
import { defineStore, acceptHMRUpdate } from 'pinia'
import * as authApi from '@/api/auth.api'
import { AccessTokenUtil, UserStorageUtil, clearAuth } from '@/utils/token.util'
import { getUserInitial } from '@/utils/user.constants'
import { createLogger } from '@/core/logger.service'
import type { UserVo, LoginDto } from '@/api/types'

const logger = createLogger('User')

export interface UserState {
  user: UserVo | null
  loading: boolean
}

export const useUserStore = defineStore('user', () => {
  const state = reactive<UserState>({
    user: null,
    loading: false,
  })

  // 启动时从 sessionStorage 恢复
  const cachedUser = UserStorageUtil.get<UserVo>()
  if (cachedUser && AccessTokenUtil.exists()) {
    state.user = cachedUser
    logger.debug('User state restored from storage')
  }

  // 计算属性
  const isLoggedIn = computed(() => !!state.user && AccessTokenUtil.exists())
  const username = computed(() => state.user?.username ?? '')
  const nickname = computed(() => state.user?.nickname || state.user?.username || '')
  const avatar = computed(() => state.user?.avatar ?? '')
  const userInitial = computed(() => getUserInitial(nickname.value))

  async function login(params: LoginDto): Promise<UserVo> {
    state.loading = true
    logger.info('Login attempt', { username: params.username })
    try {
      const result = await authApi.login(params)
      AccessTokenUtil.set(result.token)
      UserStorageUtil.set(result.user)
      state.user = result.user
      logger.info('Login successful', { userId: result.user.userId })
      return result.user
    } catch (error) {
      logger.error('Login failed', error)
      throw error
    } finally {
      state.loading = false
    }
  }

  async function logout(): Promise<void> {
    logger.info('Logout')
    try {
      await authApi.logout()
    } catch (error) {
      logger.warn('Logout API failed, clearing local state', error)
    } finally {
      state.user = null
      clearAuth()
    }
  }

  async function fetchUserInfo(): Promise<UserVo | null> {
    if (!AccessTokenUtil.exists()) {
      return null
    }
    state.loading = true
    try {
      const user = await authApi.getUserInfo()
      UserStorageUtil.set(user)
      state.user = user
      return user
    } catch (error) {
      logger.error('Failed to fetch user info', error)
      return null
    } finally {
      state.loading = false
    }
  }

  return {
    state,
    isLoggedIn,
    username,
    nickname,
    avatar,
    userInitial,
    login,
    logout,
    fetchUserInfo,
  }
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useUserStore, import.meta.hot))
}
