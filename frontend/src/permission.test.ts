import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Hoist the mock function so it's available in the factory
const mocks = vi.hoisted(() => {
  return {
    beforeEachMock: vi.fn()
  }
})

// 1. Mock router BEFORE importing permission.ts
vi.mock('@/router', () => ({
  default: {
    beforeEach: (cb: any) => mocks.beforeEachMock(cb)
  }
}))

// 2. Mock getRedirectPath
vi.mock('@/utils/redirect', () => ({
  getRedirectPath: vi.fn((roles) => {
    if (roles.includes('ROLE_ADMIN')) return '/dashboard'
    if (roles.includes('ROLE_CUSTOMER')) return '/booking'
    return '/booking'
  })
}))

// 3. Import permission.ts to register the guard
import '@/permission'
import { useUserStore } from '@/store/user'

describe('Permission Guard', () => {
  let store: any
  let guard: any

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useUserStore()
    // Retrieve the callback passed to beforeEach
    guard = mocks.beforeEachMock.mock.calls[0][0]
  })

  it('should redirect customer accessing /admin to /booking', () => {
    // Setup store state
    store.userInfo = {
      token: 'valid-token',
      roles: ['ROLE_CUSTOMER']
    }

    const to = { path: '/admin/room' }
    const from = {}
    const next = vi.fn()

    guard(to, from, next)

    expect(next).toHaveBeenCalledWith({ path: '/booking' })
  })

  it('should allow admin accessing /admin', () => {
    store.userInfo = {
      token: 'valid-token',
      roles: ['ROLE_ADMIN']
    }

    const to = { path: '/admin/room' }
    const from = {}
    const next = vi.fn()

    guard(to, from, next)

    expect(next).toHaveBeenCalledWith() // called with no args implies allowed
  })

  it('should allow tech accessing /admin', () => {
    store.userInfo = {
      token: 'valid-token',
      roles: ['ROLE_TECH']
    }

    const to = { path: '/admin/appointment' }
    const from = {}
    const next = vi.fn()

    guard(to, from, next)

    expect(next).toHaveBeenCalledWith()
  })
})
