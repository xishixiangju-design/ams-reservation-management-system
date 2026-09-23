import axios from 'axios'
import { ElMessage } from 'element-plus'

export interface ResponseResult<T = any> {
  code: number
  message: string
  data: T
}

// Create axios instance
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 5000,
  withCredentials: true // Important for Session/Cookie
})

// Request interceptor
service.interceptors.request.use(
  (config) => {
    // do something before request is sent
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (userInfo.token) {
      config.headers['Authorization'] = 'Bearer ' + userInfo.token
    }
    return config
  },
  (error) => {
    // do something with request error
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  (response) => {
    // Some APIs might return data directly without code wrapper, or with different structure
    // But based on project memory "API 响应包装规范", all APIs return ResponseResult(code/message/data)
    const res = response.data

    // If the custom code is not 200, it is judged as an error.
    // Note: Some successful responses might not have 'code' if it's a file download or legacy
    if (res.code !== undefined && res.code !== 200) {
      let msg = res.message || '未知错误'
      if (msg.includes('Internal Server Error')) {
        msg = '系统内部错误，请联系管理员'
      }
      ElMessage({
        message: msg,
        type: 'error',
        duration: 5 * 1000
      })
      // Reject with the full response object so caller can handle specific codes (e.g. 409 conflict)
      return Promise.reject(res)
    } else {
      return res
    }
  },
  (error) => {
    console.log('err' + error) // for debug
    let message = error.message
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '登录已过期，请重新登录'
          if (!window.location.pathname.includes('/login')) {
            // Clear user info
            localStorage.removeItem('userInfo')
            window.location.href = '/login'
          }
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = error.response.data?.message || error.message
      }
    }

    ElMessage({
      message: message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service
