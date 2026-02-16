/**
 * @file 菜单 API
 * @description 动态菜单相关的 API 请求
 */

import { $http } from '@/core/http.interceptor'
import type { ApiResult } from './types'
import type { MenuItem } from '@/layout/layout.types'

/**
 * 获取当前用户的菜单列表（基于 RBAC 角色过滤）
 */
export async function getUserMenus(): Promise<MenuItem[]> {
  const result = await $http.get<ApiResult<MenuItem[]>>('/system/menu/user')
  return result.data
}
