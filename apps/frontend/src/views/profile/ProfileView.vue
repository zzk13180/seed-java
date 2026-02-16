<template>
  <div class="profile-page">
    <div class="page-header">
      <h1 class="page-title">个人中心</h1>
      <p class="page-subtitle">管理您的个人信息</p>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :md="8">
        <el-card shadow="hover" class="profile-card">
          <div class="avatar-wrapper">
            <div class="avatar">{{ userInitial }}</div>
            <h2 class="user-name">{{ user?.nickname || user?.username }}</h2>
            <p class="user-role">{{ user?.roles?.[0] || '普通用户' }}</p>
          </div>
          <el-divider />
          <div class="profile-info">
            <div class="info-item">
              <span class="info-label">部门ID</span>
              <span class="info-value">{{ user?.deptId || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">邮箱</span>
              <span class="info-value">{{ user?.email || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">手机</span>
              <span class="info-value">{{ user?.phone || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">注册时间</span>
              <span class="info-value">{{ formatDate(user?.createTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="16">
        <el-card shadow="hover">
          <template #header>
            <span>基本信息</span>
          </template>
          <el-form
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="80px"
          >
            <el-form-item label="用户名">
              <el-input v-model="formData.username" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="formData.nickname" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" />
            </el-form-item>
            <el-form-item label="手机" prop="phone">
              <el-input v-model="formData.phone" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleSave">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="hover" style="margin-top: 20px">
          <template #header>
            <span>修改密码</span>
          </template>
          <el-form
            ref="passwordFormRef"
            :model="passwordData"
            :rules="passwordRules"
            label-width="100px"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordData.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordData.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordData.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user/user.store'
import * as userApi from '@/api/user.api'
import { formatDate } from '@/utils/date'
import type { FormInstance, FormRules } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const user = computed(() => userStore.state.user)
const userInitial = computed(() => userStore.userInitial)

const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const loading = ref(false)
const passwordLoading = ref(false)

const formData = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
})

const passwordData = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const formRules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordData.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

onMounted(() => {
  if (user.value) {
    Object.assign(formData, {
      username: user.value.username,
      nickname: user.value.nickname,
      email: user.value.email || '',
      phone: user.value.phone || '',
    })
  }
})

async function handleSave() {
  if (!formRef.value) {return}

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  if (!user.value) {return}

  loading.value = true
  try {
    await userApi.updateUser({
      userId: user.value.userId,
      nickname: formData.nickname,
      email: formData.email,
      phone: formData.phone,
    })
    ElMessage.success('保存成功')
    // 刷新用户信息
    await userStore.fetchUserInfo()
  } catch {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

async function handleChangePassword() {
  if (!passwordFormRef.value) {return}

  try {
    await passwordFormRef.value.validate()
  } catch {
    return
  }

  passwordLoading.value = true
  try {
    await userApi.changePassword({
      oldPassword: passwordData.oldPassword,
      newPassword: passwordData.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    await userStore.logout()
    router.push('/login')
  } catch {
    // 错误已由拦截器处理
  } finally {
    passwordLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.profile-page {
  .page-header {
    margin-bottom: 28px;
  }

  .page-title {
    font-size: 22px;
    font-weight: 600;
    margin: 0 0 4px;
    letter-spacing: -0.02em;
  }

  .page-subtitle {
    font-size: 14px;
    color: var(--seed-text-muted);
    margin: 0;
  }
}

.profile-card {
  text-align: center;
}

.avatar-wrapper {
  padding: 24px 0;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 500;
  margin: 0 auto 16px;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 2px;
  letter-spacing: -0.02em;
}

.user-role {
  font-size: 13px;
  color: var(--seed-text-muted);
  margin: 0;
}

.profile-info {
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--seed-border-light);
  font-size: 13px;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  color: var(--seed-text-muted);
}

.info-value {
  color: var(--seed-text-primary);
}
</style>
