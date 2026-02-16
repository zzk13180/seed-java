/**
 * @file HTTP 客户端单元测试
 * @description 测试 HttpClient 的核心功能，包括请求处理、拦截器、错误处理、重试机制
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { HttpClient, HttpError } from '../http.client'

describe('HttpClient', () => {
  const basePath = 'https://api.example.com'
  let client: HttpClient

  beforeEach(() => {
    client = new HttpClient(basePath)
    vi.restoreAllMocks()
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  describe('GET 请求', () => {
    it('应正确构建带查询参数的 URL（包括数组参数）', async () => {
      const mockFetch = vi.fn(async (url: string, init?: RequestInit) => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({
          url,
          method: init?.method,
          body: (init as any)?.body ?? null,
        }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const res = await client.get<any>('/users', {
        q: 'abc',
        n: 1,
        arr: [1, 2, null],
      })

      expect(res.method).toBe('GET')
      expect(res.body).toBeNull()
      expect(res.url).toContain(`${basePath}/users`)
      expect(res.url).toContain('q=abc')
      expect(res.url).toContain('n=1')
      // 数组参数应重复 key
      expect(res.url).toContain('arr=1')
      expect(res.url).toContain('arr=2')
    })

    it('应正确过滤 null 和 undefined 参数', async () => {
      const mockFetch = vi.fn(async (url: string) => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ url }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const res = await client.get<any>('/search', {
        q: 'test',
        empty: null,
        missing: undefined,
      })

      expect(res.url).toContain('q=test')
      expect(res.url).not.toContain('empty')
      expect(res.url).not.toContain('missing')
    })
  })

  describe('请求头处理', () => {
    it('应合并全局头和请求头，请求头优先级更高', async () => {
      const mockFetch = vi.fn(async (_url: string, init?: RequestInit) => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ headers: init?.headers }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      client.setHeaders({ Authorization: 'Bearer A', 'X-App': 'seed' })
      const r = await client.get<any>('/me', undefined, {
        Authorization: 'Bearer B',
      })

      const headers = r.headers as Record<string, string>
      expect(headers['Authorization']).toBe('Bearer B')
      expect(headers['X-App']).toBe('seed')
      expect(headers['Content-Type']).toBe('application/json')
    })

    it('应正确设置和清除 Authorization 头', () => {
      client.setAuthorization('test-token')
      expect(client.headers.Authorization).toBe('Bearer test-token')

      client.clearAuthorization()
      expect(client.headers.Authorization).toBeUndefined()
    })
  })

  describe('POST 请求', () => {
    it('应发送 JSON 格式的请求体', async () => {
      const mockFetch = vi.fn(async (_url: string, init?: RequestInit) => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({
          headers: init?.headers,
          method: init?.method,
          body: (init as any)?.body,
        }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const payload = { name: 'Alice', age: 25 }
      const r = await client.post<any>('/users', payload)

      expect(r.method).toBe('POST')
      const headers = r.headers as Record<string, string>
      expect(headers['Content-Type']).toBe('application/json')
      expect(r.body).toBe(JSON.stringify(payload))
    })

    it('应正确处理 FormData 上传（移除 json Content-Type）', async () => {
      // 模拟 FormData
      class MockFormData {
        private _data: Record<string, any[]> = {}
        append(k: string, v: any) {
          if (!this._data[k]) this._data[k] = []
          this._data[k].push(v)
        }
        get [Symbol.toStringTag]() {
          return 'FormData'
        }
      }
      globalThis.FormData = MockFormData as any

      const mockFetch = vi.fn(async (_url: string, init?: RequestInit) => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({
          headers: init?.headers,
          method: init?.method,
          hasBody: !!(init as any)?.body,
        }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const form = new FormData()
      form.append('file', 'dummy')

      const r = await client.upload<any>('/upload', form as any)

      expect(r.method).toBe('POST')
      const headers = r.headers as Record<string, string>
      // 上传 FormData 时不应强制 application/json
      expect(headers['Content-Type']).toBeUndefined()
      expect(r.hasBody).toBe(true)
    })
  })

  describe('错误处理', () => {
    it('应解析 JSON 格式的错误响应', async () => {
      const errorData = { code: 400, message: 'Bad Request' }
      const mockFetch = vi.fn(async () => ({
        ok: false,
        status: 400,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => errorData,
        text: async () => 'bad',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      await expect(client.get('/err')).rejects.toMatchObject({
        status: 400,
        data: errorData,
      })
    })

    it('应解析纯文本格式的错误响应', async () => {
      const mockFetch = vi.fn(async () => ({
        ok: false,
        status: 500,
        headers: new Headers({ 'content-type': 'text/plain; charset=utf-8' }),
        json: async () => ({ message: 'should not be used' }),
        text: async () => 'server error',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      await expect(client.get('/err-text')).rejects.toMatchObject({
        status: 500,
        data: 'server error',
      })
    })

    it('应在恶意 JSON 响应时抛出解析错误', async () => {
      const mockFetch = vi.fn(async () => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => {
          throw new Error('Invalid JSON')
        },
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      await expect(client.get('/bad-json')).rejects.toThrow('Invalid JSON')
    })
  })

  describe('特殊状态码处理', () => {
    it('应对 204 No Content 返回 undefined', async () => {
      const mockFetch = vi.fn(async () => ({
        ok: true,
        status: 204,
        headers: new Headers({}),
        json: async () => ({}),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const r = await client.post<void>('/no-content')
      expect(r).toBeUndefined()
    })

    it('应对 304 Not Modified 返回 undefined', async () => {
      const mockFetch = vi.fn(async () => ({
        ok: false, // 304 在 fetch 中不被认为是 ok
        status: 304,
        headers: new Headers({}),
        json: async () => ({}),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const r = await client.get<void>('/not-modified')
      expect(r).toBeUndefined()
    })

    it('应正确解析带 charset 后缀的 JSON 响应', async () => {
      const mockFetch = vi.fn(async () => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json; charset=utf-8' }),
        json: async () => ({ ok: 1 }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const r = await client.get<{ ok: number }>('/ok')
      expect(r.ok).toBe(1)
    })

    it('应对非 JSON 内容类型返回文本', async () => {
      const mockFetch = vi.fn(async () => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'text/plain' }),
        json: async () => 'not used',
        text: async () => 'hello',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const r = await client.get<string>('/text')
      expect(r).toBe('hello')
    })
  })

  describe('其他 HTTP 方法', () => {
    it('DELETE 请求可以发送 JSON body', async () => {
      const mockFetch = vi.fn(async (_url: string, init?: RequestInit) => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({
          method: init?.method,
          body: (init as any)?.body,
        }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const r = await client.delete<any>('/items/1', { force: true })
      expect(r.method).toBe('DELETE')
      expect(r.body).toBe(JSON.stringify({ force: true }))
    })

    it('PUT 请求应正确工作', async () => {
      const mockFetch = vi.fn(async (_url: string, init?: RequestInit) => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ method: init?.method }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const r = await client.put<any>('/users/1', { name: 'Bob' })
      expect(r.method).toBe('PUT')
    })

    it('PATCH 请求应正确工作', async () => {
      const mockFetch = vi.fn(async (_url: string, init?: RequestInit) => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ method: init?.method }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const r = await client.patch<any>('/users/1', { status: 'active' })
      expect(r.method).toBe('PATCH')
    })
  })

  describe('CORS 模式', () => {
    it('应始终在 fetch 选项中设置 CORS 模式', async () => {
      const mockFetch = vi.fn(async (_url: string, init?: RequestInit) => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ mode: init?.mode }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const r = await client.get<any>('/cors')
      expect(r.mode).toBe('cors')
    })
  })

  describe('拦截器', () => {
    it('应执行请求拦截器', async () => {
      const mockFetch = vi.fn(async (_url: string, init?: RequestInit) => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ headers: init?.headers }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const cleanup = client.addRequestInterceptor((config) => {
        config.headers = {
          ...config.headers,
          'X-Custom': 'test',
        }
        return config
      })

      const r = await client.get<any>('/test')
      expect((r.headers as Record<string, string>)['X-Custom']).toBe('test')

      // 测试清理函数
      cleanup()
    })

    it('应执行响应拦截器', async () => {
      const mockFetch = vi.fn(async () => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ value: 1 }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      client.addResponseInterceptor((response: any) => {
        return { ...response, intercepted: true }
      })

      const r = await client.get<any>('/test')
      expect(r.value).toBe(1)
      expect(r.intercepted).toBe(true)
    })

    it('应执行错误拦截器', async () => {
      const mockFetch = vi.fn(async () => ({
        ok: false,
        status: 401,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ message: 'Unauthorized' }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const interceptorFn = vi.fn()
      client.addErrorInterceptor((error) => {
        interceptorFn(error.status)
        throw error // 重新抛出错误
      })

      await expect(client.get('/protected')).rejects.toMatchObject({
        status: 401,
      })
      expect(interceptorFn).toHaveBeenCalledWith(401)
    })

    it('错误拦截器可以返回恢复值', async () => {
      let callCount = 0
      const mockFetch = vi.fn(async () => {
        callCount++
        if (callCount === 1) {
          return {
            ok: false,
            status: 401,
            headers: new Headers({ 'content-type': 'application/json' }),
            json: async () => ({ message: 'Unauthorized' }),
            text: async (): Promise<string> => '',
          }
        }
        return {
          ok: true,
          status: 200,
          headers: new Headers({ 'content-type': 'application/json' }),
          json: async () => ({ recovered: true }),
          text: async (): Promise<string> => '',
        }
      }) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      client.addErrorInterceptor(async (error, config) => {
        if (error.status === 401) {
          // 模拟 token 刷新后重试
          return await client.request(
            config.endpoint,
            config.method,
            config.params,
            config.headers,
          )
        }
        throw error
      })

      const r = await client.get<any>('/protected')
      expect(r.recovered).toBe(true)
      expect(callCount).toBe(2)
    })

    it('应正确移除拦截器', () => {
      const interceptor = vi.fn((config: any) => config)
      const cleanup = client.addRequestInterceptor(interceptor)

      expect((client as any).requestInterceptors).toHaveLength(1)

      cleanup()

      expect((client as any).requestInterceptors).toHaveLength(0)
    })
  })

  describe('超时和重试', () => {
    it('应在超时时抛出 HttpError', async () => {
      // 模拟 AbortError 行为
      const abortError = new Error('The operation was aborted')
      abortError.name = 'AbortError'

      const mockFetch = vi.fn(async () => {
        throw abortError
      }) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const timeoutClient = new HttpClient(basePath, { timeout: 100 })

      await expect(timeoutClient.get('/slow')).rejects.toMatchObject({
        status: 0,
        message: 'Request timeout',
      })
    })

    it('应按配置重试失败的请求', async () => {
      let callCount = 0
      const mockFetch = vi.fn(async () => {
        callCount++
        if (callCount < 3) {
          return {
            ok: false,
            status: 503,
            headers: new Headers({ 'content-type': 'application/json' }),
            json: async () => ({ message: 'Service Unavailable' }),
            text: async (): Promise<string> => '',
          }
        }
        return {
          ok: true,
          status: 200,
          headers: new Headers({ 'content-type': 'application/json' }),
          json: async () => ({ success: true }),
          text: async (): Promise<string> => '',
        }
      }) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const retryClient = new HttpClient(basePath, { retries: 3, retryDelay: 10 })
      const r = await retryClient.get<any>('/flaky')

      expect(r.success).toBe(true)
      expect(callCount).toBe(3)
    })

    it('应在达到重试上限后抛出最后的错误', async () => {
      const mockFetch = vi.fn(async () => ({
        ok: false,
        status: 503,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ message: 'Service Unavailable' }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const retryClient = new HttpClient(basePath, { retries: 2, retryDelay: 10 })

      await expect(retryClient.get('/always-fail')).rejects.toMatchObject({
        status: 503,
      })

      // 应该尝试了 1 + 2 = 3 次
      expect(mockFetch).toHaveBeenCalledTimes(3)
    })
  })

  describe('URL 构建', () => {
    it('应正确处理相对路径中的前导斜杠', async () => {
      const mockFetch = vi.fn(async (url: string) => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ url }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      // 测试带斜杠
      await client.get('/users')
      expect(mockFetch).toHaveBeenLastCalledWith(
        expect.stringContaining(`${basePath}/users`),
        expect.any(Object),
      )

      // 测试不带斜杠
      await client.get('users')
      expect(mockFetch).toHaveBeenLastCalledWith(
        expect.stringContaining(`${basePath}/users`),
        expect.any(Object),
      )
    })

    it('应正确处理 basePath 尾部斜杠', async () => {
      const clientWithSlash = new HttpClient(`${basePath}/`)
      const mockFetch = vi.fn(async (url: string) => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ url }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      await clientWithSlash.get('/users')
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringMatching(new RegExp(`^${basePath}/users`)),
        expect.any(Object),
      )
    })

    it('应正确编码特殊字符', async () => {
      const mockFetch = vi.fn(async (url: string) => ({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ url }),
        text: async () => '',
      })) as unknown as typeof fetch
      globalThis.fetch = mockFetch

      const r = await client.get<any>('/search', {
        q: 'hello world',
        special: '=&?',
      })

      // URLSearchParams 使用 + 或 %20 编码空格
      expect(r.url).toMatch(/q=hello(\+|%20)world/)
    })
  })

  describe('HttpError 类', () => {
    it('应正确创建 HttpError 实例', () => {
      const config: any = { endpoint: '/test', method: 'GET' }
      const error = new HttpError('Test error', 500, { detail: 'error' }, config)

      expect(error).toBeInstanceOf(HttpError)
      expect(error).toBeInstanceOf(Error)
      expect(error.name).toBe('HttpError')
      expect(error.message).toBe('Test error')
      expect(error.status).toBe(500)
      expect(error.data).toEqual({ detail: 'error' })
      expect(error.config).toBe(config)
    })

    it('应支持 instanceof 检查', () => {
      const error = new HttpError('Test', 400)
      expect(error instanceof HttpError).toBe(true)
      expect(error instanceof Error).toBe(true)
    })
  })
})
