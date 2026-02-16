/**
 * @file 用户管理 API
 * @description 用户管理相关的 API 请求
 */

import { $http } from '@/core/http.interceptor'
import type {
  ApiResult,
  PageResult,
  UserVo,
  UserQueryParams,
  CreateUserDto,
  UpdateUserDto,
  ResetPasswordDto,
  ChangeStatusDto,
  ChangePasswordDto,
} from './types'

const DEFAULT_PAGE_SIZE = 10

/**
 * 获取用户列表
 */
export async function getUserList(params: UserQueryParams = {}): Promise<PageResult<UserVo>> {
  const {
    pageNum = 1,
    pageSize = DEFAULT_PAGE_SIZE,
    username,
    nickname,
    phone,
    status,
  } = params

  const result = await $http.get<ApiResult<PageResult<UserVo>>>('/system/user/list', {
    pageNum,
    pageSize,
    username,
    nickname,
    phone,
    status,
  })
  return result.data
}

/**
 * 获取用户详情
 */
export async function getUserById(userId: number): Promise<UserVo> {
  const result = await $http.get<ApiResult<UserVo>>(`/system/user/${userId}`)
  return result.data
}

/**
 * 创建用户
 */
export async function createUser(data: CreateUserDto): Promise<void> {
  await $http.post('/system/user', data)
}

/**
 * 更新用户
 */
export async function updateUser(data: UpdateUserDto): Promise<void> {
  await $http.put('/system/user', data)
}

/**
 * 删除用户
 */
export async function deleteUser(userIds: number | number[]): Promise<void> {
  const ids = Array.isArray(userIds) ? userIds.join(',') : userIds
  await $http.delete(`/system/user/${ids}`)
}

/**
 * 重置用户密码
 */
export async function resetPassword(data: ResetPasswordDto): Promise<void> {
  await $http.put('/system/user/resetPwd', data)
}

/**
 * 修改用户状态
 */
export async function changeUserStatus(data: ChangeStatusDto): Promise<void> {
  await $http.put('/system/user/changeStatus', data)
}

/**
 * 修改当前用户密码
 */
export async function changePassword(data: ChangePasswordDto): Promise<void> {
  await $http.put('/system/user/profile/password', data)
}
