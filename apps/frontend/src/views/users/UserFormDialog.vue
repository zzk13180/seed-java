<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="500px"
    destroy-on-close
    @close="emit('update:modelValue', false)"
  >
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="formData.username" :disabled="isEdit" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="formData.nickname" />
      </el-form-item>
      <el-form-item v-if="!isEdit" label="密码" prop="password">
        <el-input v-model="formData.password" type="password" show-password />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="formData.email" />
      </el-form-item>
      <el-form-item label="手机" prop="phone">
        <el-input v-model="formData.phone" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="USER_STATUS.NORMAL">正常</el-radio>
          <el-radio :value="USER_STATUS.DISABLED">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit"> 确定 </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as userApi from '@/api/user.api'
import { USER_STATUS } from '@/utils/user.constants'
import type { FormInstance, FormRules } from 'element-plus'
import type { UserVo } from '@/api/types'

const props = defineProps<{
  modelValue: boolean
  editUser: UserVo | null
}>()

const emit = defineEmits<{
  'update:modelValue': [val: boolean]
  saved: []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const isEdit = computed(() => !!props.editUser)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive({
  userId: 0,
  username: '',
  nickname: '',
  password: '',
  email: '',
  phone: '',
  status: USER_STATUS.NORMAL as string,
})

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
}

watch(
  () => props.editUser,
  (user) => {
    if (user) {
      Object.assign(formData, {
        userId: user.userId,
        username: user.username,
        nickname: user.nickname,
        password: '',
        email: user.email || '',
        phone: user.phone || '',
        status: user.status,
      })
    } else {
      Object.assign(formData, {
        userId: 0,
        username: '',
        nickname: '',
        password: '',
        email: '',
        phone: '',
        status: USER_STATUS.NORMAL as string,
      })
    }
  },
  { immediate: true },
)

async function handleSubmit() {
  if (!formRef.value) {return}
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await userApi.updateUser({
        userId: formData.userId,
        username: formData.username,
        nickname: formData.nickname,
        email: formData.email,
        phone: formData.phone,
        status: formData.status,
      })
      ElMessage.success('更新成功')
    } else {
      await userApi.createUser({
        username: formData.username,
        nickname: formData.nickname,
        password: formData.password,
        email: formData.email,
        phone: formData.phone,
        status: formData.status,
      })
      ElMessage.success('创建成功')
    }
    emit('update:modelValue', false)
    emit('saved')
  } catch {
    // 错误已由拦截器处理
  } finally {
    submitLoading.value = false
  }
}
</script>
