import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import TechSelection from '../TechSelection.vue'
import { createTestingPinia } from '@pinia/testing'
import { useBookingStore } from '@/stores/booking'
import * as techApi from '@/api/technician'
import * as apptApi from '@/api/appointment'

// Mock APIs
const mockTechnicians = [
  { userId: 1, realName: 'Tech A', status: 'IDLE' },
  { userId: 2, realName: 'Tech B', status: 'BUSY' }
]

const mockSlots = [
  { startTime: '10:00:00', endTime: '11:00:00', available: true, availableTechCount: 1 },
  { startTime: '11:00:00', endTime: '12:00:00', available: false, availableTechCount: 0 }
]

vi.mock('@/api/technician', () => ({
  getTechnicianList: vi.fn(() => Promise.resolve({ code: 200, data: mockTechnicians }))
}))

vi.mock('@/api/appointment', () => ({
  getAvailableSlots: vi.fn(() => Promise.resolve(mockSlots)), // API returns array directly based on component code
  joinWaitlist: vi.fn()
}))

// Mock router
const mockRouter = {
  push: vi.fn(),
  replace: vi.fn()
}
vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRouter: () => mockRouter
  }
})

describe('TechSelection.vue', () => {
  let wrapper: any
  let store: any

  beforeEach(() => {
    vi.clearAllMocks()

    wrapper = mount(TechSelection, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {
              booking: {
                selectedServices: [{ id: 1, name: 'Service A', duration: 60, price: 100 }], // Corrected property name and type
                selectedDate: '2023-01-01',
                selectedTime: '',
                selectedTech: null,
                isAnyTech: false,
                peopleCount: 1
              }
            }
          })
        ],
        stubs: {
          'el-icon': true,
          'el-date-picker': {
            template:
              '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" @change="$emit(\'change\', $event.target.value)" />',
            props: ['modelValue']
          },
          'el-button': { template: '<button @click="$emit(\'click\')"><slot></slot></button>' },
          'el-empty': true,
          User: true
        }
      }
    })
    store = useBookingStore()
  })

  it('renders technicians list', async () => {
    await flushPromises()
    // Verify redirection didn't happen
    expect(mockRouter.replace).not.toHaveBeenCalled()

    const techCards = wrapper.findAll('.tech-card')
    // 1 (Any) + 2 (Specific)
    expect(techCards.length).toBe(3)
    expect(wrapper.text()).toContain('Tech A')
    expect(wrapper.text()).toContain('Tech B')
  })

  it('selects a technician', async () => {
    await flushPromises()
    const techCards = wrapper.findAll('.tech-card')
    // Select first specific tech (index 1)
    await techCards[1].trigger('click')

    expect(store.setTech).toHaveBeenCalledWith(expect.objectContaining({ userId: 1 }), false)
  })

  it('selects any technician', async () => {
    await flushPromises()
    const techCards = wrapper.findAll('.tech-card')
    // Select Any (index 0)
    await techCards[0].trigger('click')

    expect(store.setTech).toHaveBeenCalledWith(null, true)
  })

  it('fetches slots when date is selected', async () => {
    await flushPromises()
    // In beforeEach, selectedDate is set, so onMounted should call fetchSlots
    expect(apptApi.getAvailableSlots).toHaveBeenCalled()
  })

  it('renders time slots', async () => {
    await flushPromises()
    const slots = wrapper.findAll('.time-slot')
    expect(slots.length).toBe(2)
    expect(slots[0].text()).toContain('10:00:00')
    expect(slots[0].classes()).not.toContain('disabled')
    // Waitlist logic adds .waitlist class, check logic in component
    // Component: :class="{ waitlist: !slot.available }"
    // Slot 2 is unavailable -> waitlist
    expect(slots[1].classes()).toContain('waitlist')
  })

  it('selects a time slot', async () => {
    await flushPromises()
    const slots = wrapper.findAll('.time-slot')
    await slots[0].trigger('click')

    expect(store.setDateTime).toHaveBeenCalledWith('2023-01-01', '10:00:00')
  })
})
