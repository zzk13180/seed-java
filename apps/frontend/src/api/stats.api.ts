/**
 * @file 仪表盘统计 API
 */

import { $http } from '@/core/http.interceptor'
import type { ApiResult } from './types'

export interface DashboardStats {
  users: number
  depts: number
  roles: number
  menus: number
}

/**
 * 获取仪表盘统计数据
 */
export async function getDashboardStats(): Promise<DashboardStats> {
  const result = await $http.get<ApiResult<DashboardStats>>(
    '/system/dashboard/stats'
  )
  return result.data
}
