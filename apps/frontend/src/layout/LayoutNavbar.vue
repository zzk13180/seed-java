<template>
  <header class="navbar">
    <div class="navbar-left">
      <el-button
        class="sidebar-toggle"
        :icon="sidebarCollapsed ? Expand : Fold"
        text
        @click="toggleSidebar"
      />
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="navbar-right">
      <el-button :icon="isDarkMode ? Sunny : Moon" text @click="toggleTheme" />
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-dropdown">
          <div class="user-avatar">{{ userInitial }}</div>
          <span class="user-name">{{ nickname }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Fold, Expand, Moon, Sunny, ArrowDown } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app/app.store'
import { useUserStore } from '@/stores/user/user.store'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const sidebarCollapsed = computed(() => appStore.state.sidebarCollapsed)
const isDarkMode = computed(() => appStore.isDarkMode)
const nickname = computed(() => userStore.nickname)
const userInitial = computed(() => userStore.userInitial)

const currentTitle = computed(() => {
  return route.meta?.title as string | undefined
})

function toggleSidebar() {
  appStore.toggleSidebar()
}

function toggleTheme() {
  appStore.toggleTheme()
}

async function handleCommand(command: string) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
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
}
</script>

<style scoped lang="scss">
.navbar {
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: var(--seed-bg-color);
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.sidebar-toggle {
  font-size: 16px;
  color: var(--seed-text-muted);
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 8px;
  transition: background 0.15s;

  &:hover {
    background: var(--seed-bg-page);
  }
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  font-size: 12px;
}

.user-name {
  font-size: 13px;
  color: var(--seed-text-secondary);
}
</style>
