<template>
  <el-sub-menu v-if="item.children?.length" :index="item.path">
    <template #title>
      <el-icon v-if="item.icon">
        <component :is="getIcon(item.icon)" />
      </el-icon>
      <span>{{ item.title }}</span>
    </template>
    <LayoutSidebarItem
      v-for="child in item.children"
      :key="child.path"
      :item="child"
    />
  </el-sub-menu>

  <el-menu-item v-else :index="item.path">
    <el-icon v-if="item.icon">
      <component :is="getIcon(item.icon)" />
    </el-icon>
    <template #title>
      <span>{{ item.title }}</span>
    </template>
  </el-menu-item>
</template>

<script setup lang="ts">
import { Menu, User, Setting, Document } from '@element-plus/icons-vue'
import type { MenuItem } from './layout.types'

defineProps<{
  item: MenuItem
}>()

// 图标映射
const iconMap: Record<string, unknown> = {
  dashboard: Menu,
  user: User,
  setting: Setting,
  document: Document,
}

function getIcon(name: string) {
  return iconMap[name] || Document
}
</script>
