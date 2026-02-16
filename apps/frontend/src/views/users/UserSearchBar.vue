<template>
  <div class="search-bar">
    <el-input
      v-model="keyword"
      placeholder="搜索用户名、昵称..."
      clearable
      style="width: 240px"
      @keyup.enter="handleSearch"
    />
    <el-select v-model="status" placeholder="全部状态" clearable style="width: 120px">
      <el-option :label="USER_STATUS_LABEL[USER_STATUS.NORMAL]" :value="USER_STATUS.NORMAL" />
      <el-option :label="USER_STATUS_LABEL[USER_STATUS.DISABLED]" :value="USER_STATUS.DISABLED" />
    </el-select>
    <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
    <el-button :icon="Refresh" @click="handleReset">重置</el-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { USER_STATUS, USER_STATUS_LABEL } from '@/utils/user.constants'

const emit = defineEmits<{
  search: [params: { keyword: string; status: string }]
  reset: []
}>()

const keyword = ref('')
const status = ref('')

function handleSearch() {
  emit('search', { keyword: keyword.value, status: status.value })
}

function handleReset() {
  keyword.value = ''
  status.value = ''
  emit('reset')
}
</script>

<style scoped lang="scss">
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
}
</style>
