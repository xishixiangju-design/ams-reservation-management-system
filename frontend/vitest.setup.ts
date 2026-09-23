import { vi } from 'vitest'
import { config } from '@vue/test-utils'
import zh from './src/locales/zh'

// Helper to resolve nested keys
const resolveKey = (key: string) => {
  const keys = key.split('.')
  let current: any = zh
  for (const k of keys) {
    if (current && current[k]) {
      current = current[k]
    } else {
      return key // Fallback to key if not found
    }
  }
  return typeof current === 'string' ? current : key
}

// Mock vue-i18n
vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => resolveKey(key),
    d: (key: string) => key,
    n: (key: number) => key,
    locale: { value: 'zh-CN' }
  }),
  createI18n: () => ({
    global: {
      t: (key: string) => resolveKey(key),
      d: (key: string) => key,
      n: (key: number) => key,
      locale: { value: 'zh-CN' }
    },
    install: (app: any) => {
      app.config.globalProperties.$t = (key: string) => resolveKey(key)
      app.config.globalProperties.$d = (key: string) => key
      app.config.globalProperties.$n = (key: number) => key
      app.config.globalProperties.$i18n = { locale: 'zh-CN' }
    }
  })
}))

// Mock global $t
config.global.mocks = {
  $t: (key: string) => resolveKey(key),
  $d: (key: string) => key,
  $n: (key: number) => key
}

// Mock Axios
vi.mock('axios', () => {
  const defaultResponse = { data: { code: 200, message: 'success', data: {} } }

  // Generic request mock
  const requestMock = vi.fn((configOrUrl: any, config?: any) => {
    const url = typeof configOrUrl === 'string' ? configOrUrl : configOrUrl?.url

    // Basic routing logic for mock data
    if (url?.includes('/captcha')) {
      // Return unwrapped data because interceptors are bypassed in mock
      return Promise.resolve({
        code: 200,
        data: { image: 'data:image/png;base64,test', uuid: '123' }
      })
    }
    return Promise.resolve({ code: 200, message: 'success', data: {} })
  })

  // Create a callable function that also has methods
  const axiosInstance: any = requestMock
  axiosInstance.get = requestMock
  axiosInstance.post = requestMock
  axiosInstance.put = requestMock
  axiosInstance.delete = requestMock
  axiosInstance.request = requestMock
  axiosInstance.interceptors = {
    request: { use: vi.fn(), eject: vi.fn() },
    response: { use: vi.fn(), eject: vi.fn() }
  }
  axiosInstance.defaults = { headers: { common: {} } }

  return {
    default: {
      create: vi.fn(() => axiosInstance),
      get: requestMock,
      post: requestMock,
      put: requestMock,
      delete: requestMock,
      request: requestMock,
      defaults: { headers: { common: {} } }
    }
  }
})
