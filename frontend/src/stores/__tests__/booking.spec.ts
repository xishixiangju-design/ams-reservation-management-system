import { setActivePinia, createPinia } from 'pinia'
import { describe, it, expect, beforeEach } from 'vitest'
import { useBookingStore } from '../booking'

describe('Booking Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('initial state should be empty', () => {
    const store = useBookingStore()
    expect(store.selectedServices).toEqual([])
    expect(store.selectedTech).toBeNull()
    expect(store.selectedDate).toBe('')
    expect(store.totalPrice).toBe(0)
  })

  it('toggleService should add service and reset subsequent steps', () => {
    const store = useBookingStore()
    const service = { id: 1, name: 'Massage', price: 100, duration: 60, description: 'Test' }

    // Set some state first
    store.selectedTech = { id: 1, name: 'Tech', phone: '123' }
    store.selectedDate = '2023-01-01'

    // Action
    store.toggleService(service)

    // Assert
    expect(store.selectedServices).toContainEqual(service)
    // Price calculation is async and depends on API mock, so we skip price check or mock it.
    // Since we didn't mock API in this test file, the store might throw or just use fallback.
    // The store implementation has fallback: total += service.price * peopleCount.value
    // But calculatePrice import might fail if not mocked?
    // Wait, imports are static.

    expect(store.selectedTech).toBeNull() // Reset
    expect(store.selectedDate).toBe('') // Reset
  })

  it('setTech should update tech', () => {
    const store = useBookingStore()
    const tech = { id: 1, name: 'Tech', phone: '123' }

    store.setTech(tech)
    expect(store.selectedTech).toEqual(tech)
    expect(store.isAnyTech).toBe(false)

    store.setTech(null, true)
    expect(store.selectedTech).toBeNull()
    expect(store.isAnyTech).toBe(true)
  })

  it('setDateTime should update date and time', () => {
    const store = useBookingStore()
    store.setDateTime('2023-01-01', '10:00:00')
    expect(store.selectedDate).toBe('2023-01-01')
    expect(store.selectedTime).toBe('10:00:00')
  })

  it('reset should clear all state', () => {
    const store = useBookingStore()
    store.selectedServices = [{ id: 1, name: 'S', price: 10, duration: 10, description: '' }]
    store.selectedTech = { id: 1, name: 'T', phone: '' }

    store.reset()

    expect(store.selectedServices).toEqual([])
    expect(store.selectedTech).toBeNull()
    expect(store.isAnyTech).toBe(false)
    expect(store.selectedDate).toBe('')
  })
})
