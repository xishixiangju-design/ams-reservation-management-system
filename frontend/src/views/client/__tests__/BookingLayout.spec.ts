import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import BookingLayout from '../BookingLayout.vue'
import { createTestingPinia } from '@pinia/testing'
import { useUserStore } from '@/store/user'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

// Mock dependencies
vi.mock('vue-router', () => ({
  useRouter: vi.fn(),
  useRoute: vi.fn()
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn()
  },
  ElPopover: {
    name: 'ElPopover',
    template: '<div><slot name="reference"></slot><slot></slot></div>'
  },
  ElBadge: {
    name: 'ElBadge',
    template: '<div><slot></slot></div>',
    props: ['value', 'hidden']
  },
  ElIcon: {
    name: 'ElIcon',
    template: '<div><slot></slot></div>'
  },
  ElButton: {
    name: 'ElButton',
    template: '<button @click="$emit(\'click\')"><slot></slot></button>'
  },
  ElSteps: {
    name: 'ElSteps',
    template: '<div><slot></slot></div>'
  },
  ElStep: {
    name: 'ElStep',
    template: '<div></div>'
  }
}))

vi.mock('@element-plus/icons-vue', () => ({
  Bell: {}
}))

vi.mock('@/components/NotificationList.vue', () => ({
  default: {
    name: 'NotificationList',
    template: '<div></div>',
    methods: {
      loadData: vi.fn()
    }
  }
}))

vi.mock('@/api/notification', () => ({
  getUnreadCount: vi.fn().mockResolvedValue({ data: 5 })
}))

describe('BookingLayout.vue', () => {
  let wrapper: any
  let store: any
  let routerPush: any

  beforeEach(() => {
    routerPush = vi.fn()
    ;(useRouter as any).mockReturnValue({
      push: routerPush
    })
    ;(useRoute as any).mockReturnValue({
      path: '/booking',
      name: 'booking-service'
    })

    wrapper = mount(BookingLayout, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            stubActions: false
          })
        ],
        stubs: {
          RouterView: true,
          ElButton: {
            template: '<button @click="$emit(\'click\')"><slot></slot></button>'
          },
          ElPopover: true,
          ElBadge: true,
          ElIcon: true,
          ElSteps: true,
          ElStep: true,
          'el-button': {
            template: '<button @click="$emit(\'click\')"><slot></slot></button>'
          },
          'el-popover': true,
          'el-badge': true,
          'el-icon': true,
          'el-steps': true,
          'el-step': true
        }
      }
    })

    store = useUserStore()
    // Mock the store logout action implementation
    store.logout = vi.fn().mockResolvedValue(undefined)
  })

  it('calls userStore.logout and redirects on logout button click', async () => {
    // Find all buttons (since we stubbed ElButton to render as <button>)
    const buttons = wrapper.findAll('button')

    // Filter to find the logout button
    const logoutBtn = buttons.find((btn: any) => btn.text() === '退出登录')

    expect(logoutBtn).toBeDefined()

    await logoutBtn.trigger('click')

    expect(store.logout).toHaveBeenCalled()
    expect(ElMessage.success).toHaveBeenCalledWith('退出成功')
    expect(routerPush).toHaveBeenCalledWith('/login')
  })
})
