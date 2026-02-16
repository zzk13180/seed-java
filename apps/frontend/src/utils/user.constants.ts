/**
 * @file 用户状态常量
 * @description 集中管理用户状态码，避免 Magic String 散布在多个文件中
 */

/** 用户状态 */
export const USER_STATUS = {
  /** 正常 */
  NORMAL: '0',
  /** 停用 */
  DISABLED: '1',
} as const

/** 用户状态标签映射 */
export const USER_STATUS_LABEL: Record<string, string> = {
  [USER_STATUS.NORMAL]: '正常',
  [USER_STATUS.DISABLED]: '停用',
}

/**
 * 获取用户名首字母（大写）
 * @param name 用户名或昵称
 * @returns 首字母大写，无名字返回 '?'
 */
export function getUserInitial(name: string | undefined | null): string {
  if (!name) {
    return '?'
  }
  return name.charAt(0).toUpperCase()
}
