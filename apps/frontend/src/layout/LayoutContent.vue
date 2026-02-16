<template>
  <main class="layout-content">
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </main>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
.layout-content {
  flex: 1;
  overflow-y: auto;
  padding: 28px 32px;
  background: var(--seed-bg-page);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
