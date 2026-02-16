<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-icon">{{ icon }}</div>
      <h1 class="error-code">{{ code }}</h1>
      <p class="error-message">{{ message }}</p>
      <div class="error-actions">
        <el-button type="primary" @click="router.push('/')">返回首页</el-button>
        <el-button v-if="secondaryAction" @click="secondaryAction.handler">
          {{ secondaryAction.label }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const code = computed(() => {
  // 根据路由 meta 或 path 推断错误码
  const metaCode = route.meta?.errorCode as number | undefined
  if (metaCode) {
    return metaCode
  }
  if (route.path === '/401') {
    return 401
  }
  return 404
})

const icon = computed(() => (code.value === 401 ? '🔒' : '🌿'))
const message = computed(() =>
  code.value === 401 ? '抱歉，您没有权限访问此页面' : '抱歉，您访问的页面不存在',
)

const secondaryAction = computed(() => {
  if (code.value === 401) {
    return { label: '重新登录', handler: () => router.push('/login') }
  }
  return { label: '返回上页', handler: () => router.back() }
})
</script>

<style scoped lang="scss">
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--seed-bg-page);
}

.error-content {
  text-align: center;
}

.error-icon {
  font-size: 64px;
  margin-bottom: 12px;
  opacity: 0.8;
}

.error-code {
  font-size: 56px;
  font-weight: 700;
  color: var(--seed-text-primary);
  margin: 0;
  letter-spacing: -0.04em;
}

.error-message {
  font-size: 16px;
  color: var(--seed-text-muted);
  margin: 12px 0 40px;
}

.error-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}
</style>
