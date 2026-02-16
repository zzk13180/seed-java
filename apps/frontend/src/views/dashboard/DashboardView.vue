<template>
  <div class="dashboard-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">仪表盘</h1>
        <p class="page-subtitle">欢迎回来，{{ nickname }}</p>
      </div>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon users">👥</div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.users }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon depts">🏢</div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.depts }}</div>
            <div class="stat-label">部门数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon roles">🎭</div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.roles }}</div>
            <div class="stat-label">角色数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon menus">📋</div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.menus }}</div>
            <div class="stat-label">菜单项目</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <span>快速操作</span>
          </template>
          <div class="quick-actions">
            <el-button @click="$router.push('/users')">
              <span class="action-icon">👤</span>
              用户管理
            </el-button>
            <el-button @click="$router.push('/profile')">
              <span class="action-icon">⚙️</span>
              个人设置
            </el-button>
            <el-button @click="handleLogout">
              <span class="action-icon">🚪</span>
              退出登录
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <span>账户信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ user?.username || '-' }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ user?.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ user?.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机">{{ user?.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="部门ID">{{ user?.deptId || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="system-info-card">
      <template #header>
        <span>系统信息</span>
      </template>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="系统名称">Seed Cloud Platform</el-descriptions-item>
        <el-descriptions-item label="版本">v1.0.0</el-descriptions-item>
        <el-descriptions-item label="技术栈">Vue 3 / TypeScript / Element Plus</el-descriptions-item>
        <el-descriptions-item label="许可证">MIT License</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user/user.store'
import { getDashboardStats } from '@/api/stats.api'
import type { DashboardStats } from '@/api/stats.api'

const userStore = useUserStore()
const router = useRouter()

const nickname = computed(() => userStore.nickname)
const user = computed(() => userStore.state.user)

const stats = reactive({
  users: '--',
  depts: '--',
  roles: '--',
  menus: '--',
})

onMounted(async () => {
  try {
    const data: DashboardStats = await getDashboardStats()
    stats.users = String(data.users)
    stats.depts = String(data.depts)
    stats.roles = String(data.roles)
    stats.menus = String(data.menus)
  } catch {
    stats.users = '0'
    stats.depts = '0'
    stats.roles = '0'
    stats.menus = '0'
  }
})

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await userStore.logout()
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped lang="scss">
.dashboard-page {
  .page-header {
    margin-bottom: 32px;
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

.stats-row {
  margin-bottom: 24px;

  .el-col {
    margin-bottom: 16px;
  }
}

.stat-card {
  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    gap: 16px;
  }
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;

  &.users {
    background: #f0f9f4;
  }
  &.depts {
    background: #f0f4ff;
  }
  &.roles {
    background: #fffbeb;
  }
  &.menus {
    background: #f5f3ff;
  }
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: var(--seed-text-primary);
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 12px;
  color: var(--seed-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.action-icon {
  margin-right: 4px;
}

.system-info-card {
  margin-top: 16px;
}
</style>
