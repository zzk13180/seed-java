/**
 * @file LayoutNavbar 组件测试
 * @description 测试导航栏的渲染和交互
 */

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createApp } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import LayoutNavbar from './LayoutNavbar.vue'

// Mock stores
vi.mock('@/stores/app/app.store', () => ({
  useAppStore: vi.fn(() => ({
    state: { sidebarCollapsed: false },
    isDarkMode: false,
    toggleSidebar: vi.fn(),
    toggleTheme: vi.fn(),
  })),
}))

vi.mock('@/stores/user/user.store', () => ({
  useUserStore: vi.fn(() => ({
    nickname: '测试用户',
    username: 'testuser',
    userInitial: '测',
    logout: vi.fn(),
  })),
}))

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div />' } },
    { path: '/profile', component: { template: '<div />' } },
  ],
})

describe('LayoutNavbar', () => {
  beforeEach(() => {
    const app = createApp({})
    const pinia = createPinia()
    app.use(pinia)
    setActivePinia(pinia)
    vi.clearAllMocks()
  })

  it('should render navbar', () => {
    const wrapper = mount(LayoutNavbar, {
      global: {
        plugins: [router],
        stubs: {
          ElButton: { template: '<button><slot /></button>' },
          ElBreadcrumb: { template: '<nav><slot /></nav>' },
          ElBreadcrumbItem: { template: '<span><slot /></span>' },
          ElDropdown: { template: '<div><slot /></div>' },
          ElDropdownMenu: { template: '<div><slot /></div>' },
          ElDropdownItem: { template: '<div><slot /></div>' },
          ElIcon: { template: '<i><slot /></i>' },
        },
      },
    })

    expect(wrapper.find('.navbar').exists()).toBe(true)
    expect(wrapper.find('.navbar-left').exists()).toBe(true)
    expect(wrapper.find('.navbar-right').exists()).toBe(true)
  })

  it('should display user initial', () => {
    const wrapper = mount(LayoutNavbar, {
      global: {
        plugins: [router],
        stubs: {
          ElButton: { template: '<button><slot /></button>' },
          ElBreadcrumb: { template: '<nav><slot /></nav>' },
          ElBreadcrumbItem: { template: '<span><slot /></span>' },
          ElDropdown: { template: '<div><slot /></div>' },
          ElDropdownMenu: { template: '<div><slot /></div>' },
          ElDropdownItem: { template: '<div><slot /></div>' },
          ElIcon: { template: '<i><slot /></i>' },
        },
      },
    })

    expect(wrapper.find('.user-avatar').text()).toBe('测')
  })

  it('should display user nickname', () => {
    const wrapper = mount(LayoutNavbar, {
      global: {
        plugins: [router],
        stubs: {
          ElButton: { template: '<button><slot /></button>' },
          ElBreadcrumb: { template: '<nav><slot /></nav>' },
          ElBreadcrumbItem: { template: '<span><slot /></span>' },
          ElDropdown: { template: '<div><slot /></div>' },
          ElDropdownMenu: { template: '<div><slot /></div>' },
          ElDropdownItem: { template: '<div><slot /></div>' },
          ElIcon: { template: '<i><slot /></i>' },
        },
      },
    })

    expect(wrapper.find('.user-name').text()).toBe('测试用户')
  })
})
