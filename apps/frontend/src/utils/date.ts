/**
 * @file 日期格式化工具
 * @description 统一的日期格式化函数
 */

/**
 * 格式化日期时间（含时间）
 * @param dateStr - ISO 日期字符串
 * @returns 格式化后的日期时间字符串，如 "2024/1/15 14:30:00"
 */
export function formatDateTime(dateStr?: string | null): string {
  if (!dateStr) {
    return '-'
  }
  return new Date(dateStr).toLocaleString('zh-CN')
}

/**
 * 格式化日期（仅日期）
 * @param dateStr - ISO 日期字符串
 * @returns 格式化后的日期字符串，如 "2024/1/15"
 */
export function formatDate(dateStr?: string | null): string {
  if (!dateStr) {
    return '-'
  }
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
