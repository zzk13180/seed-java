/**
 * @file 应用状态类型定义
 */

import type { MenuItem } from '@/layout/layout.types'

/**
 * 应用状态
 */
export interface AppState {
  sidebarCollapsed: boolean
  theme: 'light' | 'dark'
  menus: MenuItem[]
  menuLoaded: boolean
}
