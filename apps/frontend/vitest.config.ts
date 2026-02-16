import { defineConfig, mergeConfig } from 'vitest/config'
import viteConfig from './vite.config'

export default mergeConfig(
  viteConfig({ mode: 'test', command: 'serve' }),
  defineConfig({
    test: {
      environment: 'happy-dom',
      globals: true,
      include: ['src/**/*.{test,spec}.{js,ts,vue}'],
      exclude: ['node_modules', 'dist', 'src/api/generated/**'],
      coverage: {
        provider: 'v8',
        reporter: ['text', 'json', 'html'],
        exclude: [
          'node_modules/**',
          'dist/**',
          'src/api/generated/**',
          '**/*.d.ts',
          '**/*.config.{js,ts}',
        ],
        thresholds: {
          // 当前实际覆盖率约 43%，目标逐步提升至 60%
          lines: 40,
          branches: 40,
          functions: 40,
          statements: 40,
        },
      },
      setupFiles: ['./src/test/setup.ts'],
      server: {
        deps: {
          inline: ['element-plus'],
        },
      },
      css: false,
    },
  })
)
