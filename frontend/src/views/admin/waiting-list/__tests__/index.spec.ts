import { describe, it, expect, vi, beforeEach } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import WaitingList from '../index.vue'
import * as api from '@/api/waitingList'
import { createI18n } from 'vue-i18n'

// Mock API
vi.mock('@/api/waitingList', () => ({
  getAdminWaitingList: vi.fn(),
  convertWaitingList: vi.fn()
}))

// Mock i18n
const i18n = createI18n({
  legacy: false,
  locale: 'en',
  messages: {
    en: {}
  }
})

describe('WaitingList.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('fetches data on mount', async () => {
    const mockData = {
      list: [
        {
          id: 1,
          customerName: 'Test User',
          customerPhone: '1234567890',
          serviceName: 'Massage',
          status: 'WAITING',
          createTime: '2023-01-01'
        }
      ],
      total: 1
    }

    // Mock API implementation
    // @ts-ignore
    vi.mocked(api.getAdminWaitingList).mockResolvedValue({
      code: 200,
      message: 'success',
      data: mockData
    })

    const wrapper = shallowMount(WaitingList, {
      global: {
        plugins: [i18n],
        directives: {
          loading: () => {}
        },
        stubs: {
          // Keep these stubbed to avoid rendering issues
          'el-table': true,
          'el-table-column': true,
          'el-pagination': true,
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-date-picker': true,
          'el-button': true,
          'el-card': true,
          'el-tag': true,
          'el-popconfirm': true
        },
        mocks: {
          $t: (key: string) => key
        }
      }
    })

    // Verify API called
    expect(api.getAdminWaitingList).toHaveBeenCalled()

    // Wait for promise resolution
    await wrapper.vm.$nextTick()
    await new Promise((resolve) => setTimeout(resolve, 0))

    // Verify state
    // @ts-ignore
    expect(wrapper.vm.total).toBe(1)
    // @ts-ignore
    expect(wrapper.vm.waitingList).toEqual(mockData.list)
  })
})
