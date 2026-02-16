/**
 * @file API 层单元测试
 * @description 测试所有 API 模块的请求函数
 */

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { $http } from '@/core/http.interceptor'

// Mock http interceptor
vi.mock('@/core/http.interceptor', () => ({
  $http: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('Auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('login', () => {
    it('should call POST /auth/login with credentials', async () => {
      const mockResponse = { code: 200, data: { token: 'test-token' } }
      vi.mocked($http.post).mockResolvedValue(mockResponse)

      const { login } = await import('../auth.api')
      await login({ username: 'admin', password: 'password' })

      expect($http.post).toHaveBeenCalledWith('/auth/login', {
        username: 'admin',
        password: 'password',
      })
    })
  })

  describe('logout', () => {
    it('should call POST /auth/logout', async () => {
      vi.mocked($http.post).mockResolvedValue({ code: 200 })

      const { logout } = await import('../auth.api')
      await logout()

      expect($http.post).toHaveBeenCalledWith('/auth/logout')
    })
  })

  describe('getUserInfo', () => {
    it('should call GET /auth/info', async () => {
      const mockUser = { userId: 1, username: 'admin', nickname: 'Admin' }
      vi.mocked($http.get).mockResolvedValue({
        code: 200,
        data: { user: mockUser, roles: [], permissions: [] },
      })

      const { getUserInfo } = await import('../auth.api')
      await getUserInfo()

      expect($http.get).toHaveBeenCalledWith('/auth/info')
    })
  })

})

describe('Menu API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getUserMenus', () => {
    it('should call GET /system/menu/user', async () => {
      const mockMenus = [
        { path: '/dashboard', name: 'Dashboard', meta: { title: '仪表盘' } },
        { path: '/users', name: 'Users', meta: { title: '用户管理' } },
      ]
      vi.mocked($http.get).mockResolvedValue({ code: 200, data: mockMenus })

      const { getUserMenus } = await import('../menu.api')
      await getUserMenus()

      expect($http.get).toHaveBeenCalledWith('/system/menu/user')
    })
  })
})

describe('Stats API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getDashboardStats', () => {
    it('should call GET /system/dashboard/stats', async () => {
      const mockStats = { users: 100, depts: 10, roles: 5, menus: 20 }
      vi.mocked($http.get).mockResolvedValue({ code: 200, data: mockStats })

      const { getDashboardStats } = await import('../stats.api')
      await getDashboardStats()

      expect($http.get).toHaveBeenCalledWith('/system/dashboard/stats')
    })
  })
})

describe('User API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getUserList', () => {
    it('should call GET /system/user/list with default pagination', async () => {
      const mockResponse = {
        code: 200,
        data: {
          pageNum: 1,
          pageSize: 10,
          total: 100,
          pages: 10,
          records: [{ userId: 1, username: 'user1' }],
        },
      }
      vi.mocked($http.get).mockResolvedValue(mockResponse)

      const { getUserList } = await import('../user.api')
      await getUserList()

      expect($http.get).toHaveBeenCalledWith('/system/user/list', {
        pageNum: 1,
        pageSize: 10,
        username: undefined,
        nickname: undefined,
        phone: undefined,
        status: undefined,
      })
    })

    it('should pass query parameters', async () => {
      vi.mocked($http.get).mockResolvedValue({
        code: 200,
        data: { pageNum: 2, pageSize: 20, total: 50, pages: 3, records: [] },
      })

      const { getUserList } = await import('../user.api')
      await getUserList({ pageNum: 2, pageSize: 20, username: 'admin', status: '1' })

      expect($http.get).toHaveBeenCalledWith('/system/user/list', {
        pageNum: 2,
        pageSize: 20,
        username: 'admin',
        nickname: undefined,
        phone: undefined,
        status: '1',
      })
    })
  })

  describe('getUserById', () => {
    it('should call GET /system/user/:id', async () => {
      const mockUser = { userId: 1, username: 'admin' }
      vi.mocked($http.get).mockResolvedValue({ code: 200, data: mockUser })

      const { getUserById } = await import('../user.api')
      await getUserById(1)

      expect($http.get).toHaveBeenCalledWith('/system/user/1')
    })
  })

  describe('createUser', () => {
    it('should call POST /system/user', async () => {
      vi.mocked($http.post).mockResolvedValue({ code: 200 })

      const { createUser } = await import('../user.api')
      const userData = { username: 'newuser', nickname: 'New User', password: 'password123' }
      await createUser(userData)

      expect($http.post).toHaveBeenCalledWith('/system/user', userData)
    })
  })

  describe('updateUser', () => {
    it('should call PUT /system/user', async () => {
      vi.mocked($http.put).mockResolvedValue({ code: 200 })

      const { updateUser } = await import('../user.api')
      const userData = { userId: 1, nickname: 'Updated Name' }
      await updateUser(userData)

      expect($http.put).toHaveBeenCalledWith('/system/user', userData)
    })
  })

  describe('deleteUser', () => {
    it('should call DELETE /system/user/:id for single user', async () => {
      vi.mocked($http.delete).mockResolvedValue({ code: 200 })

      const { deleteUser } = await import('../user.api')
      await deleteUser(1)

      expect($http.delete).toHaveBeenCalledWith('/system/user/1')
    })

    it('should call DELETE /system/user/:ids for multiple users', async () => {
      vi.mocked($http.delete).mockResolvedValue({ code: 200 })

      const { deleteUser } = await import('../user.api')
      await deleteUser([1, 2, 3])

      expect($http.delete).toHaveBeenCalledWith('/system/user/1,2,3')
    })
  })

  describe('resetPassword', () => {
    it('should call PUT /system/user/resetPwd', async () => {
      vi.mocked($http.put).mockResolvedValue({ code: 200 })

      const { resetPassword } = await import('../user.api')
      await resetPassword({ userId: 1, password: 'newpassword' })

      expect($http.put).toHaveBeenCalledWith('/system/user/resetPwd', {
        userId: 1,
        password: 'newpassword',
      })
    })
  })

  describe('changeUserStatus', () => {
    it('should call PUT /system/user/changeStatus', async () => {
      vi.mocked($http.put).mockResolvedValue({ code: 200 })

      const { changeUserStatus } = await import('../user.api')
      await changeUserStatus({ userId: 1, status: '0' })

      expect($http.put).toHaveBeenCalledWith('/system/user/changeStatus', {
        userId: 1,
        status: '0',
      })
    })
  })

  describe('changePassword', () => {
    it('should call PUT /system/user/profile/password', async () => {
      vi.mocked($http.put).mockResolvedValue({ code: 200 })

      const { changePassword } = await import('../user.api')
      await changePassword({ oldPassword: 'old', newPassword: 'new' })

      expect($http.put).toHaveBeenCalledWith('/system/user/profile/password', {
        oldPassword: 'old',
        newPassword: 'new',
      })
    })
  })
})
