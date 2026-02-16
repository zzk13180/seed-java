import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from './user.store'
import type { UserVo, LoginResult } from '@/api/types'

const mockUser: UserVo = {
  userId: 1,
  username: 'admin',
  nickname: '管理员',
  avatar: '',
  status: '1',
  roles: [],
}
const mockLoginResult: LoginResult = { token: 'tok-123', user: mockUser }

vi.mock('@/api/auth.api', () => ({
  login: vi.fn(),
  logout: vi.fn(),
  getUserInfo: vi.fn(),
}))

vi.mock('@/utils/token.util', () => ({
  AccessTokenUtil: { exists: vi.fn(() => false), set: vi.fn(), remove: vi.fn() },
  UserStorageUtil: { get: vi.fn(() => null), set: vi.fn(), remove: vi.fn() },
  clearAuth: vi.fn(),
}))

vi.mock('@/core/logger.service', () => ({
  createLogger: () => ({ debug: vi.fn(), info: vi.fn(), warn: vi.fn(), error: vi.fn() }),
}))

// eslint-disable-next-line import/first, import/order
import * as authApi from '@/api/auth.api'
// eslint-disable-next-line import/first, import/order
import { AccessTokenUtil, clearAuth } from '@/utils/token.util'

describe('useUserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('初始状态为未登录', () => {
    const store = useUserStore()
    expect(store.isLoggedIn).toBe(false)
    expect(store.username).toBe('')
    expect(store.nickname).toBe('')
  })

  describe('login', () => {
    it('登录成功后更新状态', async () => {
      vi.mocked(authApi.login).mockResolvedValue(mockLoginResult)
      const store = useUserStore()
      vi.mocked(AccessTokenUtil.exists).mockReturnValue(true)

      const user = await store.login({ username: 'admin', password: '123456' })

      expect(user).toEqual(mockUser)
      expect(store.state.user).toEqual(mockUser)
      expect(store.username).toBe('admin')
      expect(store.nickname).toBe('管理员')
      expect(store.state.loading).toBe(false)
    })

    it('登录失败时抛出异常', async () => {
      vi.mocked(authApi.login).mockRejectedValue(new Error('fail'))
      const store = useUserStore()

      await expect(store.login({ username: 'x', password: 'x' })).rejects.toThrow('fail')
      expect(store.state.user).toBeNull()
      expect(store.state.loading).toBe(false)
    })
  })

  describe('logout', () => {
    it('登出后清除状态', async () => {
      vi.mocked(authApi.logout).mockResolvedValue(undefined)
      const store = useUserStore()
      store.state.user = mockUser

      await store.logout()

      expect(store.state.user).toBeNull()
      expect(clearAuth).toHaveBeenCalled()
    })

    it('API 失败仍清除本地状态', async () => {
      vi.mocked(authApi.logout).mockRejectedValue(new Error('network'))
      const store = useUserStore()
      store.state.user = mockUser

      await store.logout()

      expect(store.state.user).toBeNull()
      expect(clearAuth).toHaveBeenCalled()
    })
  })

  describe('fetchUserInfo', () => {
    it('无 token 返回 null', async () => {
      vi.mocked(AccessTokenUtil.exists).mockReturnValue(false)
      const store = useUserStore()

      expect(await store.fetchUserInfo()).toBeNull()
      expect(authApi.getUserInfo).not.toHaveBeenCalled()
    })

    it('有 token 时获取并缓存用户信息', async () => {
      vi.mocked(AccessTokenUtil.exists).mockReturnValue(true)
      vi.mocked(authApi.getUserInfo).mockResolvedValue(mockUser)
      const store = useUserStore()

      const user = await store.fetchUserInfo()

      expect(user).toEqual(mockUser)
      expect(store.state.user).toEqual(mockUser)
    })
  })

  describe('userInitial', () => {
    it('从昵称首字符计算', async () => {
      vi.mocked(authApi.login).mockResolvedValue(mockLoginResult)
      vi.mocked(AccessTokenUtil.exists).mockReturnValue(true)
      const store = useUserStore()
      await store.login({ username: 'admin', password: '123456' })

      expect(store.userInitial).toBe('管')
    })
  })
})
