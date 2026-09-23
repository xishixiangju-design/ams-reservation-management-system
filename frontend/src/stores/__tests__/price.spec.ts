import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { useBookingStore } from '../booking'

// Mock API
vi.mock('@/api/service', () => ({
  getServiceList: vi.fn()
}))

vi.mock('@/api/pricing', () => ({
  calculatePrice: vi.fn().mockImplementation(() => {
    // Return undefined to force fallback logic which uses simple multiplication
    return Promise.resolve({ data: undefined })
  })
}))

describe('Booking Store Price Precision', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should handle floating point precision correctly', async () => {
    const store = useBookingStore()

    // Scenario 1: 0.1 + 0.2 = 0.30000000000000004
    // We simulate this by having two services with prices 0.1 and 0.2
    const service1 = { id: 1, name: 'S1', price: 0.1, duration: 10, description: '' }
    const service2 = { id: 2, name: 'S2', price: 0.2, duration: 10, description: '' }

    store.selectedServices = [service1, service2]
    store.peopleCount = 1

    // Wait for watch callback
    await new Promise((resolve) => setTimeout(resolve, 10))

    expect(store.totalPrice).toBe(0.3)
  })

  it('should handle complex floating point precision', async () => {
    const store = useBookingStore()

    // Scenario 2: 1.05 * 3 = 3.1500000000000004
    const service = { id: 3, name: 'S3', price: 1.05, duration: 10, description: '' }

    store.selectedServices = [service]
    store.peopleCount = 3

    // Wait for watch callback
    await new Promise((resolve) => setTimeout(resolve, 10))

    expect(store.totalPrice).toBe(3.15)
  })

  it('should handle the specific case from user screenshot', async () => {
    const store = useBookingStore()

    // The user saw 734.4000000000001
    // Let's try to reproduce a case that might lead to this.
    // E.g. 367.2 * 2 = 734.4
    // In JS: 367.2 * 2 === 734.4 (True)
    // Maybe it was addition of multiple items?
    // 168 + 328 + ...

    // Let's just verify that if we forcefully set a value that results in that,
    // the store rounds it.
    // Since we can't easily reproduce the exact combination without knowing the services,
    // we rely on the generic precision test above.

    // But let's try a case: 10.12 + 10.23 = 20.35
    const s1 = { id: 4, name: 'S4', price: 10.12, duration: 10 }
    const s2 = { id: 5, name: 'S5', price: 10.23, duration: 10 }

    store.selectedServices = [s1, s2]

    await new Promise((resolve) => setTimeout(resolve, 10))

    expect(store.totalPrice).toBe(20.35)
  })
})
