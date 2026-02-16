/**
 * @file TheLogo 组件测试
 * @description 测试 Logo 组件的渲染和折叠状态
 */

import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import TheLogo from './TheLogo.vue'

describe('TheLogo', () => {
  it('should render with default props', () => {
    const wrapper = mount(TheLogo)

    expect(wrapper.find('.logo').exists()).toBe(true)
    expect(wrapper.find('.logo-icon').text()).toBe('🌱')
    expect(wrapper.find('.logo-text').text()).toBe('Seed Cloud')
  })

  it('should display custom title', () => {
    const wrapper = mount(TheLogo, {
      props: { title: 'My App' },
    })

    expect(wrapper.find('.logo-text').text()).toBe('My App')
  })

  it('should hide text when collapsed', () => {
    const wrapper = mount(TheLogo, {
      props: { collapsed: true },
    })

    expect(wrapper.find('.logo').classes()).toContain('collapsed')
    expect(wrapper.find('.logo-text').exists()).toBe(false)
  })

  it('should show text when not collapsed', () => {
    const wrapper = mount(TheLogo, {
      props: { collapsed: false },
    })

    expect(wrapper.find('.logo').classes()).not.toContain('collapsed')
    expect(wrapper.find('.logo-text').exists()).toBe(true)
  })
})
