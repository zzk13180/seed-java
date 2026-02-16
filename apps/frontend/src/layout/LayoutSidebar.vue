<template>
  <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
    <div class="sidebar-header">
      <div class="sidebar-logo">
        <TheLogo :collapsed="sidebarCollapsed" />
      </div>
    </div>

    <el-scrollbar class="sidebar-menu">
      <el-menu
        :default-active="currentPath"
        :collapse="sidebarCollapsed"
        :collapse-transition="false"
        router
        class="sidebar-el-menu"
      >
        <template v-for="item in menuItems" :key="item.path">
          <LayoutSidebarItem :item="item" />
        </template>
      </el-menu>
    </el-scrollbar>

    <div class="sidebar-footer">
      <div v-show="!sidebarCollapsed" class="user-info">
        <div class="user-avatar">{{ userInitial }}</div>
        <div class="user-details">
          <div class="user-name">{{ nickname }}</div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app/app.store'
import { useUserStore } from '@/stores/user/user.store'
import TheLogo from '@/components/TheLogo.vue'
import LayoutSidebarItem from './LayoutSidebarItem.vue'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const sidebarCollapsed = computed(() => appStore.state.sidebarCollapsed)
const currentPath = computed(() => route.path)
const nickname = computed(() => userStore.nickname)
const userInitial = computed(() => userStore.userInitial)

const menuItems = computed(() => appStore.menus)

onMounted(async () => {
  await appStore.loadMenus()
})
</script>

<style scoped lang="scss">
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 220px;
  background: var(--seed-bg-color);
  display: flex;
  flex-direction: column;
  transition: width 0.2s ease;
  z-index: 1000;

  &.collapsed {
    width: 64px;
  }
}

.sidebar-header {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 18px;
}

.sidebar-logo {
  display: flex;
  align-items: center;
}

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  padding-top: 8px;
}

.sidebar-el-menu {
  border-right: none;

  &:not(.el-menu--collapse) {
    width: 220px;
  }
}

.sidebar-footer {
  padding: 16px 18px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  font-size: 13px;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--seed-text-secondary);
}

@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);

    &.show {
      transform: translateX(0);
    }
  }
}
</style>
