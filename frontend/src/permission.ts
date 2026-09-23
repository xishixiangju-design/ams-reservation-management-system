import router from './router'
import { useUserStore } from '@/store/user'
import { storeToRefs } from 'pinia'
import { getRedirectPath } from '@/utils/redirect'

const whiteList = ['/login', '/register', '/reset-password']

router.beforeEach((to, from, next) => {
  // useUserStore must be called inside the guard to ensure Pinia is active
  const userStore = useUserStore()
  const { userInfo } = storeToRefs(userStore)
  const hasToken = userInfo.value.token

  if (hasToken) {
    if (to.path === '/login') {
      // If logged in and trying to access login, redirect to home page based on role
      const roles = userInfo.value.roles || []
      const redirectPath = getRedirectPath(roles)
      next({ path: redirectPath })
    } else {
      // Access Control for Admin Routes
      if (to.path.startsWith('/admin')) {
        const roles = userInfo.value.roles || []
        const hasAdminAccess = roles.some((role) =>
          ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_TECH'].includes(role)
        )

        if (!hasAdminAccess) {
          // User does not have permission, redirect to their home page
          const redirectPath = getRedirectPath(roles)
          next({ path: redirectPath })
          return
        }
      }
      next()
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})
