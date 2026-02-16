<template>
  <svg :class="svgClass" aria-hidden="true">
    <use :xlink:href="iconName" />
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    name: string
    color?: string
    size?: string | number
  }>(),
  {
    color: 'currentColor',
    size: '1em',
  }
)

const iconName = computed(() => `#icon-${props.name}`)

const svgClass = computed(() => {
  return ['svg-icon', props.name ? `icon-${props.name}` : '']
})

const svgStyle = computed(() => ({
  width: typeof props.size === 'number' ? `${props.size}px` : props.size,
  height: typeof props.size === 'number' ? `${props.size}px` : props.size,
  color: props.color,
}))
</script>

<style scoped>
.svg-icon {
  width: v-bind('svgStyle.width');
  height: v-bind('svgStyle.height');
  color: v-bind('svgStyle.color');
  fill: currentColor;
  vertical-align: middle;
}
</style>
