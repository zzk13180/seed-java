/**
 * @file API 类型定义
 * @description 定义 API 请求和响应的类型
 */

/**
 * 通用 API 响应格式
 */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

/**
 * 分页响应格式
 */
export interface PageResult<T = unknown> {
  pageNum: number
  pageSize: number
  total: number
  pages: number
  records: T[]
}

/**
 * 登录请求参数
 */
export interface LoginDto {
  username: string
  password: string
}

/**
 * 登录响应
 */
export interface LoginResult {
  token: string
  tokenName?: string
  /** 登录时返回用户信息 */
  user: UserVo
}

/**
 * 用户信息（与后端 LoginUser 对应）
 */
export interface UserVo {
  userId: number
  username: string
  nickname: string
  email?: string
  phone?: string
  avatar?: string
  status?: string
  createTime?: string
  deptId?: number
  tenantId?: string
  roles?: string[]
  permissions?: string[]
}

/**
 * 获取用户信息响应
 */
export interface UserInfoResult {
  user: UserVo
  roles: string[]
  permissions: string[]
}

/**
 * 用户列表查询参数
 */
export interface UserQueryParams {
  pageNum?: number
  pageSize?: number
  username?: string
  nickname?: string
  phone?: string
  status?: string
}

/**
 * 创建用户请求
 */
export interface CreateUserDto {
  username: string
  nickname: string
  password: string
  email?: string
  phone?: string
  status?: string
  deptId?: number
  roleIds?: number[]
}

/**
 * 更新用户请求
 */
export interface UpdateUserDto {
  userId: number
  username?: string
  nickname?: string
  email?: string
  phone?: string
  status?: string
  deptId?: number
  roleIds?: number[]
}

/**
 * 重置密码请求
 */
export interface ResetPasswordDto {
  userId: number
  password: string
}

/**
 * 修改状态请求
 */
export interface ChangeStatusDto {
  userId: number
  status: string
}

/**
 * 修改密码请求
 */
export interface ChangePasswordDto {
  oldPassword: string
  newPassword: string
}
