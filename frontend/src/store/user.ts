import { defineStore } from 'pinia'
import { login, logout } from '@/api/auth'
import type { LoginData, LoginResult } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}') as Partial<LoginResult>
  }),
  actions: {
    async login(loginForm: LoginData) {
      try {
        // Backend returns Result<LoginResult>, so res is { code, message, data: LoginResult }
        const res = (await login(loginForm)) as unknown as { data: LoginResult }
        const data = res.data
        this.userInfo = data

        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        return res
      } catch (error) {
        throw error
      }
    },
    async logout() {
      try {
        await logout()
      } catch (e) {
        console.error(e)
      } finally {
        this.userInfo = {}
        localStorage.removeItem('userInfo')
        // Session cookie is cleared by browser or ignored after expiration/invalidation
      }
    }
  }
})
