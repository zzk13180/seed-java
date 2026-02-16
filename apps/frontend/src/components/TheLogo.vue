<template>
  <div class="logo" :class="{ collapsed }">
    <span class="logo-icon">🌱</span>
    <span v-if="!collapsed" class="logo-text">{{ title }}</span>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    collapsed?: boolean
  }>(),
  {
    title: 'Seed Cloud',
    collapsed: false,
  }
)
</script>

<style scoped lang="scss">
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  font-size: 16px;
  color: var(--seed-text-primary);
  white-space: nowrap;
  overflow: hidden;
  letter-spacing: -0.02em;

  &.collapsed {
    justify-content: center;
  }
}

.logo-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.logo-text {
  transition: opacity 0.2s ease;
}
</style>
