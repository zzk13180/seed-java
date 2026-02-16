/**
 * @file 布局类型定义
 */

/**
 * 菜单项
 */
export interface MenuItem {
  path: string
  title: string
  icon?: string
  children?: MenuItem[]
  meta?: {
    hideMenu?: boolean
    requiresAuth?: boolean
  }
}
