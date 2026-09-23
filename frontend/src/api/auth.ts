import service from '@/utils/request'

export interface LoginData {
  username: string
  password: string // Required
  captchaUuid: string
  userInputCaptcha: string
}

export interface RegisterData {
  username: string
  password: string // Required
  nickname?: string
  email?: string
  captchaUuid: string
  userInputCaptcha: string
}

export interface LoginResult {
  id: number
  username: string
  nickname: string
  roles: string[]
  token: string
}

export interface CaptchaResult {
  uuid: string
  image: string
}

export const getCaptcha = () => {
  return service<any, CaptchaResult>({
    url: '/captcha',
    method: 'get'
  })
}

export const login = (data: LoginData) => {
  return service<any, LoginResult>({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export const register = (data: RegisterData) => {
  return service<any, void>({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export interface ResetPasswordData {
  email: string
  code: string
  newPassword: string
}

export const sendResetCode = (email: string) => {
  return service<any, void>({
    url: '/auth/send-reset-code',
    method: 'post',
    data: { email }
  })
}

export const resetPassword = (data: ResetPasswordData) => {
  return service<any, void>({
    url: '/auth/reset-password',
    method: 'post',
    data
  })
}

export const logout = () => {
  return service<any, void>({
    url: '/auth/logout',
    method: 'post'
  })
}
