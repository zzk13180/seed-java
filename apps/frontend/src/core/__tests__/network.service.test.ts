/**
 * @file 网络状态监听服务测试
 * @description 测试网络状态变化监听和用户通知
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ElMessage } from 'element-plus'
import {
  setupNetworkListener,
  isNetworkOnline,
  removeNetworkListener,
} from '../network.service'

// Mock element-plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    warning: vi.fn(),
    error: vi.fn(),
  },
}))

// Mock logger service
vi.mock('@/core/logger.service', () => ({
  createLogger: vi.fn(() => ({
    info: vi.fn(),
    debug: vi.fn(),
    warn: vi.fn(),
    error: vi.fn(),
  })),
}))

describe('NetworkService', () => {
  let addEventListenerSpy: ReturnType<typeof vi.spyOn>
  let removeEventListenerSpy: ReturnType<typeof vi.spyOn>
  let eventListeners: Record<string, EventListener[]>

  beforeEach(() => {
    vi.clearAllMocks()

    // Track event listeners
    eventListeners = {
      online: [],
      offline: [],
    }

    addEventListenerSpy = vi.spyOn(window, 'addEventListener').mockImplementation(
      (type: string, listener: EventListenerOrEventListenerObject) => {
        if (type in eventListeners && typeof listener === 'function') {
          eventListeners[type].push(listener)
        }
      }
    )

    removeEventListenerSpy = vi.spyOn(window, 'removeEventListener').mockImplementation(
      (type: string, listener: EventListenerOrEventListenerObject) => {
        if (type in eventListeners && typeof listener === 'function') {
          const index = eventListeners[type].indexOf(listener)
          if (index > -1) {
            eventListeners[type].splice(index, 1)
          }
        }
      }
    )

    // Mock navigator.onLine
    Object.defineProperty(navigator, 'onLine', {
      value: true,
      writable: true,
      configurable: true,
    })
  })

  afterEach(() => {
    addEventListenerSpy.mockRestore()
    removeEventListenerSpy.mockRestore()
    vi.resetModules()
  })

  describe('setupNetworkListener', () => {
    it('should register online and offline event listeners', () => {
      setupNetworkListener()

      expect(addEventListenerSpy).toHaveBeenCalledWith('online', expect.any(Function))
      expect(addEventListenerSpy).toHaveBeenCalledWith('offline', expect.any(Function))
    })
  })

  describe('removeNetworkListener', () => {
    it('should remove online and offline event listeners', () => {
      setupNetworkListener()
      removeNetworkListener()

      expect(removeEventListenerSpy).toHaveBeenCalledWith('online', expect.any(Function))
      expect(removeEventListenerSpy).toHaveBeenCalledWith('offline', expect.any(Function))
    })
  })

  describe('isNetworkOnline', () => {
    it('should return current network status', () => {
      // Default should be true (navigator.onLine = true)
      expect(isNetworkOnline()).toBe(true)
    })
  })

  describe('Network state changes', () => {
    it('should show warning message when going offline', async () => {
      vi.resetModules()
      const module = await import('../network.service')
      module.setupNetworkListener()

      // Trigger offline event
      const offlineListener = eventListeners['offline'][0]
      expect(offlineListener).toBeDefined()
      offlineListener(new Event('offline'))

      expect(ElMessage.warning).toHaveBeenCalledWith({
        message: '网络连接已断开，请检查网络设置',
        duration: 5000,
      })
    })

    it('should show success message when coming back online after being offline', async () => {
      vi.resetModules()
      const module = await import('../network.service')
      module.setupNetworkListener()

      // First go offline
      const offlineListener = eventListeners['offline'][0]
      offlineListener(new Event('offline'))

      // Then come back online
      const onlineListener = eventListeners['online'][0]
      onlineListener(new Event('online'))

      expect(ElMessage.success).toHaveBeenCalledWith('网络已恢复连接')
    })

    it('should not show success message if was never offline', async () => {
      vi.resetModules()
      const module = await import('../network.service')
      module.setupNetworkListener()

      // Trigger online without having been offline
      const onlineListener = eventListeners['online'][0]
      onlineListener(new Event('online'))

      expect(ElMessage.success).not.toHaveBeenCalled()
    })

    it('should update isNetworkOnline status when offline', async () => {
      vi.resetModules()
      const module = await import('../network.service')
      module.setupNetworkListener()

      expect(module.isNetworkOnline()).toBe(true)

      // Trigger offline
      const offlineListener = eventListeners['offline'][0]
      offlineListener(new Event('offline'))

      expect(module.isNetworkOnline()).toBe(false)
    })

    it('should update isNetworkOnline status when back online', async () => {
      vi.resetModules()
      const module = await import('../network.service')
      module.setupNetworkListener()

      // Go offline first
      const offlineListener = eventListeners['offline'][0]
      offlineListener(new Event('offline'))
      expect(module.isNetworkOnline()).toBe(false)

      // Come back online
      const onlineListener = eventListeners['online'][0]
      onlineListener(new Event('online'))
      expect(module.isNetworkOnline()).toBe(true)
    })
  })
})
