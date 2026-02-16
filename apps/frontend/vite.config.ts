import path from 'node:path'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import compression from 'vite-plugin-compression'
import tailwindcss from '@tailwindcss/vite'

import type { UserConfig, ConfigEnv } from 'vite'

export default defineConfig(({ mode }: ConfigEnv): UserConfig => {
  const env = loadEnv(mode, import.meta.dirname)
  const workspaceRoot = path.resolve(import.meta.dirname, '../..')

  const apiTarget = env.VITE_API_URL || 'http://127.0.0.1:8080'

  const proxy = {
    [env['VITE_API_BASE_PATH'] || '/api']: {
      target: apiTarget,
      changeOrigin: true,
      ws: true,
      rewrite: (path: string) =>
        path.replace(new RegExp(`^${env['VITE_API_BASE_PATH'] || '/api'}`), ''),
    },
  }

  return {
    root: import.meta.dirname,
    base: '/',
    resolve: {
      alias: {
        '@': path.resolve(import.meta.dirname, './src'),
      },
    },
    server: {
      proxy,
      host: '0.0.0.0',
      port: 5173,
      fs: {
        allow: [workspaceRoot],
      },
    },
    preview: {
      proxy,
    },
    css: {
      postcss: {
        plugins: [
          {
            postcssPlugin: 'internal:charset-removal',
            AtRule: {
              charset: (atRule) => {
                if (atRule.name === 'charset') {
                  atRule.remove()
                }
              },
            },
          },
        ],
      },
    },
    plugins: [
      vue(),
      tailwindcss(),
      AutoImport({
        imports: ['vue', 'vue-router', 'pinia'],
        resolvers: [ElementPlusResolver()],
        dts: path.resolve(import.meta.dirname, 'src/auto-imports.d.ts'),
        eslintrc: {
          enabled: true,
          filepath: path.resolve(import.meta.dirname, '.eslintrc-auto-import.json'),
        },
      }),
      Components({
        resolvers: [ElementPlusResolver()],
        dts: path.resolve(import.meta.dirname, 'src/components.d.ts'),
      }),
      createSvgIconsPlugin({
        iconDirs: [path.resolve(import.meta.dirname, 'src/assets/svg-icons')],
        symbolId: 'icon-[dir]-[name]',
      }),
      // Gzip 压缩
      compression({
        algorithm: 'gzip',
        ext: '.gz',
        threshold: 10240, // 只压缩大于 10KB 的文件
      }),
    ],
    optimizeDeps: {
      include: ['vue', 'vue-router', 'pinia', 'element-plus'],
    },
    build: {
      target: 'esnext',
      outDir: 'dist',
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          manualChunks: {
            'vue-vendor': ['vue', 'vue-router', 'pinia'],
            'element-plus': ['element-plus'],
          },
        },
      },
    },
  }
})
