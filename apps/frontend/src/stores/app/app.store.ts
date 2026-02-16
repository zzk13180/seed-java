/**
 * @file 应用状态管理
 * @description 全局应用状态，包含侧边栏、主题、菜单等
 */

import { reactive, computed } from 'vue'
import { defineStore, acceptHMRUpdate } from 'pinia'
import { getUserMenus } from '@/api/menu.api'
import { createLogger } from '@/core/logger.service'
import type { MenuItem } from '@/layout/layout.types'
import type { AppState } from './app.types'

const logger = createLogger('AppStore')

/** 默认菜单（后端不可用时的回退） */
const DEFAULT_MENUS: MenuItem[] = [
  {
    path: '/dashboard',
    title: '仪表盘',
    icon: 'dashboard',
  },
  {
    path: '/users',
    title: '用户管理',
    icon: 'user',
  },
]

/**
 * 应用状态管理 Store
 */
export const useAppStore = defineStore('app', () => {
  // 响应式状态（sidebarCollapsed 和 theme 由 pinia-plugin-persistedstate 自动恢复）
  const state = reactive<AppState>({
    sidebarCollapsed: false,
    theme: 'light',
    menus: DEFAULT_MENUS,
    menuLoaded: false,
  })

  // 计算属性
  const isDarkMode = computed(() => state.theme === 'dark')

  // 切换侧边栏
  function toggleSidebar(): void {
    state.sidebarCollapsed = !state.sidebarCollapsed
  }

  // 设置侧边栏状态
  function setSidebarCollapsed(collapsed: boolean): void {
    state.sidebarCollapsed = collapsed
  }

  // 切换主题
  function toggleTheme(): void {
    state.theme = state.theme === 'light' ? 'dark' : 'light'
    applyTheme()
  }

  // 设置主题
  function setTheme(theme: 'light' | 'dark'): void {
    state.theme = theme
    applyTheme()
  }

  // 应用主题到 DOM
  function applyTheme(): void {
    if (state.theme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  // 初始化时应用主题
  applyTheme()

  // 菜单管理
  const menus = computed(() => state.menus)

  /**
   * 加载用户菜单（带缓存，防止重复加载）
   */
  async function loadMenus(force = false): Promise<MenuItem[]> {
    if (state.menuLoaded && !force) {
      return state.menus
    }

    try {
      const remoteMenus = await getUserMenus()
      if (remoteMenus && remoteMenus.length > 0) {
        state.menus = remoteMenus
      }
      state.menuLoaded = true
      logger.info('Menus loaded successfully')
    } catch (error) {
      logger.warn('Failed to load menus, using defaults', error)
      state.menus = DEFAULT_MENUS
      state.menuLoaded = true
    }

    return state.menus
  }

  /**
   * 重置菜单状态（登出时调用）
   */
  function resetMenus(): void {
    state.menus = DEFAULT_MENUS
    state.menuLoaded = false
  }

  return {
    state,
    isDarkMode,
    menus,
    toggleSidebar,
    setSidebarCollapsed,
    toggleTheme,
    setTheme,
    loadMenus,
    resetMenus,
  }
}, {
  persist: {
    pick: ['state.sidebarCollapsed', 'state.theme'],
    key: 'seed_cloud_app',
  },
})

// HMR 支持
if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useAppStore, import.meta.hot))
}
