import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/booking/service' // Redirect to booking for now, or /login if strict
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/Login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/register/Register.vue')
    },
    {
      path: '/reset-password',
      name: 'reset-password',
      component: () => import('@/views/login/ResetPassword.vue')
    },
    {
      path: '/dashboard',
      redirect: '/admin/dashboard'
    },
    {
      path: '/admin',
      component: () => import('@/layout/AdminLayout.vue'),
      children: [
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('@/views/admin/dashboard/index.vue')
        },
        {
          path: 'service',
          name: 'admin-service',
          component: () => import('@/views/admin/service/index.vue')
        },
        {
          path: 'tech-mgt',
          name: 'admin-technician',
          component: () => import('@/views/admin/technician/index.vue')
        },
        {
          path: 'room',
          name: 'admin-room',
          component: () => import('@/views/admin/room/index.vue')
        },
        {
          path: 'appointment',
          name: 'admin-appointment',
          component: () => import('@/views/admin/appointment/index.vue')
        },
        {
          path: 'appointment/create',
          name: 'admin-appointment-create',
          component: () => import('@/views/admin/appointment/create.vue')
        },
        {
          path: 'appointment/detail/:id',
          name: 'admin-appointment-detail',
          component: () => import('@/views/admin/appointment/detail.vue')
        },
        {
          path: 'customer',
          name: 'admin-customer',
          component: () => import('@/views/admin/customer/index.vue')
        },
        {
          path: 'waiting-list',
          name: 'admin-waiting-list',
          component: () => import('@/views/admin/waiting-list/index.vue')
        },
        {
          path: 'logs',
          name: 'admin-logs',
          component: () => import('@/views/admin/logs/index.vue')
        },
        // Attendance Management
        {
          path: 'attendance/leave',
          name: 'admin-leave-management',
          component: () => import('@/views/attendance/LeaveManagement.vue')
        },
        {
          path: 'attendance/record',
          name: 'admin-record-management',
          component: () => import('@/views/attendance/RecordManagement.vue')
        },
        // Technician Routes
        {
          path: 'technician/my-appointments',
          name: 'technician-my-appointments',
          component: () => import('@/views/admin/technician/MyAppointments.vue')
        },
        {
          path: 'technician/leave',
          name: 'technician-leave',
          component: () => import('@/views/admin/technician/LeaveRequest.vue')
        },
        {
          path: 'technician/attendance',
          name: 'technician-attendance',
          component: () => import('@/views/admin/technician/Attendance.vue')
        },
        {
          path: '',
          redirect: 'dashboard'
        }
      ]
    },
    {
      path: '/booking',
      component: () => import('@/views/client/BookingLayout.vue'),
      children: [
        {
          path: 'service',
          name: 'booking-service',
          component: () => import('@/views/client/ServiceSelection.vue')
        },
        {
          path: 'tech',
          name: 'booking-tech',
          component: () => import('@/views/client/TechSelection.vue')
        },
        {
          path: 'confirm',
          name: 'booking-confirm',
          component: () => import('@/views/client/Confirmation.vue')
        },
        {
          path: 'success',
          name: 'booking-success',
          component: () => import('@/views/client/Success.vue')
        },
        {
          path: '',
          redirect: { name: 'booking-service' }
        }
      ]
    },
    {
      path: '/payment',
      component: () => import('@/views/client/BookingLayout.vue'),
      children: [
        {
          path: 'result',
          name: 'payment-result',
          component: () => import('@/views/client/PaymentResult.vue')
        }
      ]
    },
    {
      path: '/my-appointments',
      component: () => import('@/views/client/BookingLayout.vue'),
      children: [
        {
          path: '',
          name: 'my-appointments',
          component: () => import('@/views/client/MyAppointments.vue')
        }
      ]
    },
    {
      path: '/member-center',
      component: () => import('@/views/client/BookingLayout.vue'),
      children: [
        {
          path: '',
          name: 'member-center',
          component: () => import('@/views/client/MemberCenter.vue')
        }
      ]
    }
  ]
})

export default router
