<template>
  <el-table v-loading="loading" :data="data" stripe style="width: 100%">
    <el-table-column prop="userId" label="ID" width="80" />
    <el-table-column prop="username" label="用户名" min-width="120" />
    <el-table-column prop="nickname" label="昵称" min-width="120" />
    <el-table-column prop="email" label="邮箱" min-width="180" />
    <el-table-column prop="phone" label="手机" min-width="140" />
    <el-table-column label="状态" width="100">
      <template #default="{ row }">
        <el-switch
          v-model="row.status"
          :active-value="USER_STATUS.NORMAL"
          :inactive-value="USER_STATUS.DISABLED"
          @change="emit('statusChange', row)"
        />
      </template>
    </el-table-column>
    <el-table-column prop="createTime" label="创建时间" width="180">
      <template #default="{ row }">
        {{ formatDateTime(row.createTime) }}
      </template>
    </el-table-column>
    <el-table-column label="操作" width="200" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" @click="emit('edit', row)">编辑</el-button>
        <el-button link type="primary" @click="emit('resetPassword', row)">重置密码</el-button>
        <el-button link type="danger" @click="emit('delete', row)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>

  <!-- 分页 -->
  <div class="pagination-wrapper">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="currentPageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { USER_STATUS } from '@/utils/user.constants'
import { formatDateTime } from '@/utils/date'
import type { UserVo } from '@/api/types'

const props = defineProps<{
  data: UserVo[]
  loading: boolean
  total: number
  pageNum: number
  pageSize: number
}>()

const emit = defineEmits<{
  edit: [row: UserVo]
  delete: [row: UserVo]
  resetPassword: [row: UserVo]
  statusChange: [row: UserVo]
  'update:pageNum': [val: number]
  'update:pageSize': [val: number]
  pageChange: []
  sizeChange: []
}>()

const currentPage = computed({
  get: () => props.pageNum,
  set: (val) => emit('update:pageNum', val),
})

const currentPageSize = computed({
  get: () => props.pageSize,
  set: (val) => emit('update:pageSize', val),
})

function handlePageChange() {
  emit('pageChange')
}

function handleSizeChange() {
  emit('sizeChange')
}
</script>

<style scoped lang="scss">
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--seed-border-light);
}
</style>
