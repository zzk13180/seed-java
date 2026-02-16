/**
 * ESLint 配置文件 (Flat Config)
 *
 * 针对 Vue 3 + TypeScript 前端项目的 ESLint 配置
 * 参考项目统一规范，集成：
 * - @typescript-eslint: TypeScript 静态分析
 * - eslint-plugin-vue: Vue.js 规范
 * - eslint-plugin-import: 模块导入规范
 * - auto-import: 自动导入的全局变量
 */

import globals from 'globals'
import js from '@eslint/js'
import tsEslintPlugin from '@typescript-eslint/eslint-plugin'
import tsEslintParser from '@typescript-eslint/parser'
import importPlugin from 'eslint-plugin-import'
import vuePlugin from 'eslint-plugin-vue'
import vueParser from 'vue-eslint-parser'
import autoImportGlobals from './.eslintrc-auto-import.json' with { type: 'json' }

export default [
  // ----- 基础配置 -----
  js.configs.recommended,

  // ----- 忽略目录 -----
  {
    ignores: [
      'dist/',
      'public/',
      'node_modules/',
      'src/api/generated/',
      'src/auto-imports.d.ts',
      'src/components.d.ts',
    ],
  },

  // ----- 全局配置 -----
  {
    plugins: {
      import: importPlugin,
    },
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: {
        ...globals.browser,
        ...globals.node,
        ...autoImportGlobals.globals,
      },
    },
    rules: {
      // 代码质量
      'no-debugger': 'error',
      eqeqeq: ['error', 'always', { null: 'ignore' }],
      'no-var': 'error',
      'prefer-const': ['error', { destructuring: 'all' }],
      'prefer-template': 'error',
      'prefer-arrow-callback': 'error',
      'object-shorthand': ['error', 'always', { avoidQuotes: true }],
      'one-var': ['error', 'never'],
      curly: 'error',
      'no-empty': ['error', { allowEmptyCatch: true }],

      // 代码风格
      'no-floating-decimal': 'error',

      // 错误预防
      'no-unreachable': 'error',
      'no-caller': 'error',
      'no-eval': 'error',
      'no-implied-eval': 'error',
      'no-extend-native': 'error',
      'no-extra-bind': 'error',
      'no-template-curly-in-string': 'error',
      'array-callback-return': 'error',

      // 最佳实践
      'no-alert': 'error',
      'no-new-func': 'error',
      'no-new-wrappers': 'error',
      'no-return-assign': 'error',
      'no-self-compare': 'error',
      'no-throw-literal': 'error',
      'no-useless-call': 'error',
      'no-useless-concat': 'error',
      'no-useless-return': 'error',
      radix: 'error',
      yoda: 'error',

      'max-depth': ['error', 5],
      'max-params': ['error', 5],
      'no-nested-ternary': 'error',
      'no-unneeded-ternary': 'error',

      // Import 规则
      'import/first': 'error',
      'import/no-absolute-path': 'error',
      'import/no-mutable-exports': 'error',
      'import/newline-after-import': 'error',
      'import/order': [
        'error',
        {
          groups: [
            'builtin',
            'external',
            'internal',
            'parent',
            'sibling',
            'index',
            'object',
            'type',
          ],
          pathGroups: [
            { pattern: '@seed/**', group: 'internal', position: 'before' },
            { pattern: '@/**', group: 'internal', position: 'after' },
          ],
          pathGroupsExcludedImportTypes: ['type'],
        },
      ],
    },
  },

  // ----- TypeScript 文件配置 -----
  {
    files: ['**/*.ts'],
    plugins: {
      '@typescript-eslint': tsEslintPlugin,
    },
    languageOptions: {
      parser: tsEslintParser,
      parserOptions: {
        projectService: true,
      },
    },
    rules: {
      ...tsEslintPlugin.configs.recommended.rules,

      // ========== 类型安全规则 ==========
      // 严格类型检查，确保类型安全
      'no-use-before-define': 'off',
      '@typescript-eslint/no-unused-expressions': 'off',
      '@typescript-eslint/no-unsafe-member-access': 'error', // 访问 any 类型成员
      '@typescript-eslint/no-unsafe-assignment': 'error',    // 赋值 any 类型
      '@typescript-eslint/no-unsafe-argument': 'error',      // 传递 any 类型参数
      '@typescript-eslint/no-unsafe-call': 'error',          // 调用 any 类型
      '@typescript-eslint/no-empty-function': 'off',
      '@typescript-eslint/no-explicit-any': 'error',         // 禁止显式 any
      '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }],
      '@typescript-eslint/ban-ts-comment': 'error',          // 禁止 @ts-ignore
      '@typescript-eslint/no-non-null-assertion': 'error',   // 禁止 ! 断言
      '@typescript-eslint/no-empty-object-type': 'off',

      // 类型导入
      '@typescript-eslint/consistent-type-imports': ['error', { disallowTypeAnnotations: true }],

      // 代码质量
      '@typescript-eslint/no-use-before-define': ['error', { functions: false, classes: false }],
      '@typescript-eslint/prefer-optional-chain': 'error',
    },
  },

  // ----- Vue 文件配置 -----
  {
    files: ['**/*.vue'],
    plugins: { vue: vuePlugin },
    languageOptions: {
      parser: vueParser,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
        parser: tsEslintParser,
      },
    },
    rules: {
      ...vuePlugin.configs['flat/recommended'].rules,

      // Vue 组件规范规则
      'vue/multi-word-component-names': 'error',    // 组件名必须为多单词
      'vue/require-default-prop': 'error',          // Props 必须有默认值
      'vue/require-explicit-emits': 'error',        // 必须显式声明 emits
      'vue/no-v-html': 'error',                     // 禁止 v-html 防止 XSS
      'no-unused-vars': ['error', { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }],
    },
  },

  // ----- 测试文件配置 -----
  {
    files: ['**/*.spec.ts', '**/*.test.ts'],
    languageOptions: {
      globals: {
        describe: 'readonly',
        it: 'readonly',
        test: 'readonly',
        expect: 'readonly',
        vi: 'readonly',
        beforeEach: 'readonly',
        afterEach: 'readonly',
        beforeAll: 'readonly',
        afterAll: 'readonly',
      },
    },
    rules: {
      '@typescript-eslint/no-floating-promises': 'off',
      '@typescript-eslint/no-misused-promises': 'off',
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-unsafe-assignment': 'off',
      '@typescript-eslint/no-unsafe-member-access': 'off',
      '@typescript-eslint/no-unsafe-argument': 'off',
      '@typescript-eslint/no-unsafe-call': 'off',
      curly: 'off',
    },
  },
]
