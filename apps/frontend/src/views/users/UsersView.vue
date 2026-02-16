<template>
  <div class="users-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-subtitle">管理系统中的所有用户账户</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog"> 新增用户 </el-button>
    </div>

    <el-card shadow="hover">
      <UserSearchBar @search="handleSearch" @reset="handleReset" />
      <UserTable
        :data="userList"
        :loading="loading"
        :total="pagination.total"
        v-model:page-num="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        @edit="openEditDialog"
        @delete="handleDelete"
        @reset-password="handleResetPassword"
        @status-change="handleStatusChange"
        @page-change="loadUserList"
        @size-change="handleSizeChange"
      />
    </el-card>

    <UserFormDialog
      v-model="dialogVisible"
      :edit-user="editingUser"
      @saved="loadUserList"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import * as userApi from '@/api/user.api'
import { USER_STATUS } from '@/utils/user.constants'
import UserSearchBar from './UserSearchBar.vue'
import UserTable from './UserTable.vue'
import UserFormDialog from './UserFormDialog.vue'
import type { UserVo } from '@/api/types'

const loading = ref(false)
const dialogVisible = ref(false)
const editingUser = ref<UserVo | null>(null)
const userList = ref<UserVo[]>([])

const searchParams = reactive({ keyword: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

onMounted(() => loadUserList())

async function loadUserList() {
  loading.value = true
  try {
    const result = await userApi.getUserList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      username: searchParams.keyword || undefined,
      status: searchParams.status || undefined,
    })
    userList.value = result.records
    pagination.total = result.total
  } catch {
    userList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

function handleSearch(params: { keyword: string; status: string }) {
  searchParams.keyword = params.keyword
  searchParams.status = params.status
  pagination.pageNum = 1
  loadUserList()
}

function handleReset() {
  searchParams.keyword = ''
  searchParams.status = ''
  pagination.pageNum = 1
  loadUserList()
}

function handleSizeChange() {
  pagination.pageNum = 1
  loadUserList()
}

function openCreateDialog() {
  editingUser.value = null
  dialogVisible.value = true
}

function openEditDialog(row: UserVo) {
  editingUser.value = row
  dialogVisible.value = true
}

async function handleStatusChange(row: UserVo) {
  const status = row.status ?? USER_STATUS.NORMAL
  try {
    await userApi.changeUserStatus({ userId: row.userId, status })
    ElMessage.success('状态更新成功')
  } catch {
    row.status = status === USER_STATUS.NORMAL ? USER_STATUS.DISABLED : USER_STATUS.NORMAL
  }
}

async function handleResetPassword(row: UserVo) {
  try {
    const { value } = await ElMessageBox.prompt('请输入新密码', '重置密码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^.{6,20}$/,
      inputErrorMessage: '密码长度在 6 到 20 个字符',
    })
    await userApi.resetPassword({ userId: row.userId, password: value })
    ElMessage.success('密码重置成功')
  } catch {
    // 用户取消
  }
}

async function handleDelete(row: UserVo) {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await userApi.deleteUser(row.userId)
    ElMessage.success('删除成功')
    loadUserList()
  } catch {
    // 用户取消
  }
}
</script>

<style scoped lang="scss">
.users-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 28px;
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
</style>
