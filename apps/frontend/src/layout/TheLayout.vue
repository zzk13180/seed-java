<template>
  <div class="layout">
    <LayoutSidebar />
    <div class="layout-main" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <LayoutNavbar />
      <LayoutContent />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/stores/app/app.store'
import LayoutSidebar from './LayoutSidebar.vue'
import LayoutNavbar from './LayoutNavbar.vue'
import LayoutContent from './LayoutContent.vue'

const appStore = useAppStore()
const sidebarCollapsed = computed(() => appStore.state.sidebarCollapsed)
</script>

<style scoped lang="scss">
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--seed-bg-page);
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  margin-left: 220px;
  transition: margin-left 0.2s ease;

  &.sidebar-collapsed {
    margin-left: 64px;
  }
}

@media (max-width: 768px) {
  .layout-main {
    margin-left: 0;
  }
}
</style>
