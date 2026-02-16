/**
 * @file 认证 API
 * @description 用户认证相关的 API 请求
 */

import { $http } from '@/core/http.interceptor'
import type { ApiResult, LoginDto, LoginResult, UserVo, UserInfoResult } from './types'

/**
 * 用户登录
 */
export async function login(params: LoginDto): Promise<LoginResult> {
  const result = await $http.post<ApiResult<LoginResult>>('/auth/login', params)
  return result.data
}

/**
 * 用户登出
 */
export async function logout(): Promise<void> {
  await $http.post('/auth/logout')
}

/**
 * 获取当前用户信息
 */
export async function getUserInfo(): Promise<UserVo> {
  const result = await $http.get<ApiResult<UserInfoResult>>('/auth/info')
  return result.data.user
}
