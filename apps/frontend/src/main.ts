import '@/assets/styles/tailwindcss.css'
import '@/assets/styles/index.scss'
import 'virtual:svg-icons-register'

import { createApp, type Component } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

import SvgIcon from '@/components/SvgIcon.vue'
import App from './App.vue'
import { router } from './pages/router'
import { setupHttpInterceptors } from './core/http.interceptor'
import { setupNetworkListener, removeNetworkListener } from './core/network.service'
import { AccessTokenUtil } from './utils/token.util'
import { createLogger } from './core/logger.service'

/**
 * 确保 favicon 指向 SVG 版本
 */
function ensureFavicon(): void {
  const link = document.querySelector("link[rel*='icon']")
  if (link instanceof HTMLLinkElement) {
    link.type = 'image/svg+xml'
    link.href = '/favicon.svg'
    document.head.append(link)
  }
}

function bootstrap(): void {
  const app = createApp(App as Component)
  const pinia = createPinia()
  pinia.use(piniaPluginPersistedstate)

  const logger = createLogger('App')

  app.config.errorHandler = (err, _instance, info) => {
    logger.error('Vue component error', { error: err, info })
  }

  window.addEventListener('unhandledrejection', (event: PromiseRejectionEvent) => {
    logger.error('Unhandled Promise rejection', { reason: String(event.reason) })
    event.preventDefault()
  })

  // HttpOnly Cookie 模式：从 sessionStorage 缓存恢复认证标记
  AccessTokenUtil.restoreFromUserCache()

  setupHttpInterceptors()
  setupNetworkListener()

  app.use(pinia)
  app.use(router)
  app.component('SvgIcon', SvgIcon)
  app.mount('#app')

  if (import.meta.env.DEV) {
    console.log('🌱 Seed Cloud started in development mode')
  }
}

document.addEventListener('DOMContentLoaded', ensureFavicon)
bootstrap()

if (import.meta.hot) {
  import.meta.hot.dispose(() => {
    removeNetworkListener()
  })
}
