import { describe, it, expect } from 'vitest'
import { parseError, ErrorType } from '../error.service'
import { HttpError } from '../http.client'

describe('parseError', () => {
  describe('HttpError 分类', () => {
    const cases: [number, ErrorType, boolean][] = [
      [401, ErrorType.UNAUTHORIZED, true],
      [403, ErrorType.FORBIDDEN, false],
      [404, ErrorType.NOT_FOUND, false],
      [400, ErrorType.CLIENT_ERROR, false],
      [422, ErrorType.CLIENT_ERROR, false],
      [500, ErrorType.SERVER_ERROR, false],
      [502, ErrorType.SERVICE_UNAVAILABLE, false],
      [503, ErrorType.SERVICE_UNAVAILABLE, false],
      [504, ErrorType.SERVICE_UNAVAILABLE, false],
    ]

    it.each(cases)('HTTP %i → %s (reLogin=%s)', (status, expectedType, expectedReLogin) => {
      const result = parseError(new HttpError('', status))
      expect(result.type).toBe(expectedType)
      expect(result.requiresReLogin).toBe(expectedReLogin)
      expect(result.status).toBe(status)
    })
  })

  describe('网络和超时', () => {
    it('status=0 → NETWORK_ERROR', () => {
      expect(parseError(new HttpError('Failed to fetch', 0)).type).toBe(ErrorType.NETWORK_ERROR)
    })

    it('status=0 + timeout message → TIMEOUT_ERROR', () => {
      expect(parseError(new HttpError('Request timeout', 0)).type).toBe(ErrorType.TIMEOUT_ERROR)
    })

    it('TypeError → NETWORK_ERROR', () => {
      expect(parseError(new TypeError('Failed to fetch')).type).toBe(ErrorType.NETWORK_ERROR)
    })

    it('AbortError → TIMEOUT_ERROR', () => {
      const err = new Error('aborted')
      err.name = 'AbortError'
      expect(parseError(err).type).toBe(ErrorType.TIMEOUT_ERROR)
    })
  })

  describe('服务器消息提取', () => {
    it.each(['message', 'msg', 'error', 'detail'])(
      '从 response.data.%s 提取', (field) => {
        const err = new HttpError('', 400, { [field]: '自定义消息' })
        expect(parseError(err).message).toBe('自定义消息')
      },
    )

    it('无服务器消息时使用默认消息', () => {
      const result = parseError(new HttpError('', 404))
      expect(result.title).toBe('资源不存在')
      expect(result.message).toBeTruthy()
    })
  })

  describe('ErrorInfo 结构', () => {
    it('401 标记 requiresReLogin=true', () => {
      expect(parseError(new HttpError('', 401)).requiresReLogin).toBe(true)
    })

    it('非 401 requiresReLogin=false', () => {
      expect(parseError(new HttpError('', 403)).requiresReLogin).toBe(false)
      expect(parseError(new HttpError('', 500)).requiresReLogin).toBe(false)
    })

    it('普通 Error → UNKNOWN_ERROR', () => {
      const result = parseError(new Error('Something went wrong'))
      expect(result.type).toBe(ErrorType.UNKNOWN_ERROR)
      expect(result.status).toBe(0)
    })
  })
})
