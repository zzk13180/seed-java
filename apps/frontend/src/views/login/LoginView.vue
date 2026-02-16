<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <div class="login-logo">
          <span class="logo-icon">🌱</span>
          <span class="logo-text">Seed Cloud</span>
        </div>
        <h1 class="login-title">欢迎回来</h1>
        <p class="login-subtitle">登录您的账户以继续</p>
      </div>

      <el-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="formState.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="formState.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            :prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="login-options">
          <el-checkbox v-model="formState.remember">记住我</el-checkbox>
          <el-link type="primary" :underline="false">忘记密码？</el-link>
        </div>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="formState.loading"
            @click="handleLogin"
          >
            {{ formState.loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p class="login-footer-text">
          还没有账户？<el-link type="primary" :underline="false">联系管理员</el-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user/user.store'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()

const formState = reactive<{ username: string; password: string; remember: boolean; loading: boolean }>({
  username: import.meta.env.DEV ? (import.meta.env.VITE_DEFAULT_USERNAME || '') : '',
  password: import.meta.env.DEV ? (import.meta.env.VITE_DEFAULT_PASSWORD || '') : '',
  remember: false,
  loading: false,
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
}

async function handleLogin() {
  if (!formRef.value) {return}

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  formState.loading = true

  try {
    await userStore.login({ username: formState.username, password: formState.password })

    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
    // 错误已由 HTTP 拦截器处理
  } finally {
    formState.loading = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--seed-bg-page);
}

.login-container {
  width: 100%;
  max-width: 380px;
  padding: 48px 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 32px;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: var(--seed-text-primary);
  letter-spacing: -0.02em;
}

.login-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 8px;
  color: var(--seed-text-primary);
  letter-spacing: -0.02em;
}

.login-subtitle {
  font-size: 14px;
  color: var(--seed-text-muted);
  margin: 0;
}

.login-form {
  margin-top: 28px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 15px;
}

.login-footer {
  text-align: center;
  margin-top: 32px;
}

.login-footer-text {
  font-size: 13px;
  color: var(--seed-text-muted);
  margin: 0;
}
</style>
