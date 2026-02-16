/**
 * @file LoginView 测试
 * @description 测试登录页面的渲染和交互
 */

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createApp } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import LoginView from './LoginView.vue'

// Mock user store
const mockLogin = vi.fn()
vi.mock('@/stores/user/user.store', () => ({
  useUserStore: vi.fn(() => ({
    login: mockLogin,
    isLoggedIn: false,
  })),
}))

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div />' } },
    { path: '/login', component: LoginView },
  ],
})

describe('LoginView', () => {
  beforeEach(() => {
    const app = createApp({})
    const pinia = createPinia()
    app.use(pinia)
    setActivePinia(pinia)
    vi.clearAllMocks()
  })

  it('should render login form', () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [router],
        stubs: {
          ElForm: { template: '<form><slot /></form>' },
          ElFormItem: { template: '<div><slot /></div>' },
          ElInput: { template: '<input />' },
          ElButton: { template: '<button><slot /></button>' },
          ElCheckbox: { template: '<label><input type="checkbox" /><slot /></label>' },
          ElLink: { template: '<a><slot /></a>' },
        },
      },
    })

    expect(wrapper.find('.login-page').exists()).toBe(true)
    expect(wrapper.find('.login-container').exists()).toBe(true)
  })

  it('should display welcome text', () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [router],
        stubs: {
          ElForm: { template: '<form><slot /></form>' },
          ElFormItem: { template: '<div><slot /></div>' },
          ElInput: { template: '<input />' },
          ElButton: { template: '<button><slot /></button>' },
          ElCheckbox: { template: '<label><input type="checkbox" /><slot /></label>' },
          ElLink: { template: '<a><slot /></a>' },
        },
      },
    })

    expect(wrapper.find('.login-title').text()).toBe('欢迎回来')
  })

  it('should display Seed Cloud logo', () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [router],
        stubs: {
          ElForm: { template: '<form><slot /></form>' },
          ElFormItem: { template: '<div><slot /></div>' },
          ElInput: { template: '<input />' },
          ElButton: { template: '<button><slot /></button>' },
          ElCheckbox: { template: '<label><input type="checkbox" /><slot /></label>' },
          ElLink: { template: '<a><slot /></a>' },
        },
      },
    })

    expect(wrapper.find('.logo-text').text()).toBe('Seed Cloud')
  })

  it('should call login on form submission', async () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [router],
        stubs: {
          ElForm: {
            props: ['model', 'rules'],
            template: '<form @submit.prevent><slot /></form>',
            methods: {
              validate() { return Promise.resolve(true) },
            },
          },
          ElFormItem: { template: '<div><slot /></div>' },
          ElInput: {
            props: ['modelValue'],
            emits: ['update:modelValue'],
            template: `<input :value="modelValue" @input="$emit('update:modelValue', $event.target.value)" />`,
          },
          ElButton: { template: '<button><slot /></button>' },
          ElCheckbox: { template: '<label><input type="checkbox" /><slot /></label>' },
          ElLink: { template: '<a><slot /></a>' },
        },
      },
    })

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('admin')
    await inputs[1].setValue('password123')

    await wrapper.find('.login-button').trigger('click')
    await flushPromises()

    expect(mockLogin).toHaveBeenCalledWith({
      username: 'admin',
      password: 'password123',
    })
  })
})
