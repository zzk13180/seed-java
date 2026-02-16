import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import DashboardView from './DashboardView.vue'

// Mock stats API
vi.mock('@/api/stats.api', () => ({
  getDashboardStats: vi.fn().mockResolvedValue({
    users: 42,
    depts: 5,
    roles: 3,
    menus: 12,
  }),
}))

// Mock user store
vi.mock('@/stores/user/user.store', () => ({
  useUserStore: vi.fn(() => ({
    nickname: '管理员',
    state: {
      user: {
        userId: 1,
        username: 'admin',
        nickname: '管理员',
        email: 'admin@example.com',
        phone: '13800138000',
        deptId: 1,
      },
    },
    logout: vi.fn(),
  })),
}))

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/', component: { template: '<div />' } },
    { path: '/users', component: { template: '<div />' } },
    { path: '/profile', component: { template: '<div />' } },
  ],
})

describe('DashboardView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  function mountDashboard() {
    return mount(DashboardView, {
      global: {
        plugins: [router],
        stubs: {
          ElRow: { template: '<div class="el-row"><slot /></div>' },
          ElCol: { template: '<div class="el-col"><slot /></div>' },
          ElCard: { template: '<div class="el-card"><slot /><slot name="header" /></div>' },
          ElButton: { template: '<button><slot /></button>' },
          ElDescriptions: { template: '<div class="el-descriptions"><slot /></div>' },
          ElDescriptionsItem: {
            props: ['label'],
            template: '<div class="el-descriptions-item"><span>{{ label }}</span><slot /></div>',
          },
        },
      },
    })
  }

  it('应正确渲染仪表盘页面', () => {
    const wrapper = mountDashboard()
    expect(wrapper.find('.dashboard-page').exists()).toBe(true)
    expect(wrapper.find('.page-title').text()).toBe('仪表盘')
  })

  it('应显示欢迎信息包含用户昵称', () => {
    const wrapper = mountDashboard()
    expect(wrapper.find('.page-subtitle').text()).toContain('管理员')
  })

  it('应渲染统计卡片区域', () => {
    const wrapper = mountDashboard()
    expect(wrapper.findAll('.el-card').length).toBeGreaterThan(0)
    expect(wrapper.text()).toContain('用户总数')
    expect(wrapper.text()).toContain('部门数量')
    expect(wrapper.text()).toContain('角色数量')
    expect(wrapper.text()).toContain('菜单项目')
  })

  it('应在加载完成后显示统计数值', async () => {
    const wrapper = mountDashboard()
    await flushPromises()

    const statValues = wrapper.findAll('.stat-value')
    expect(statValues).toHaveLength(4)
    expect(statValues[0].text()).toBe('42')
    expect(statValues[1].text()).toBe('5')
    expect(statValues[2].text()).toBe('3')
    expect(statValues[3].text()).toBe('12')
  })

  it('应渲染快速操作区域', () => {
    const wrapper = mountDashboard()
    expect(wrapper.text()).toContain('快速操作')
  })

  it('应渲染账户信息区域', () => {
    const wrapper = mountDashboard()
    expect(wrapper.text()).toContain('账户信息')
  })

  it('应渲染系统信息', () => {
    const wrapper = mountDashboard()
    expect(wrapper.text()).toContain('Seed Cloud Platform')
  })
})
