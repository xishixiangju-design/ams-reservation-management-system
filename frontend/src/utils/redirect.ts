/**
 * 获取角色对应的默认跳转路径
 * @param roles 用户角色列表
 * @returns 跳转路径
 */
export const getRedirectPath = (roles: string[]): string => {
  if (!roles || roles.length === 0) {
    return '/booking'
  }

  // 优先级：管理员 > 技师 > 普通用户
  if (roles.includes('ROLE_ADMIN') || roles.includes('ROLE_MANAGER')) {
    return '/dashboard'
  }

  if (roles.includes('ROLE_TECH')) {
    return '/admin/technician/my-appointments'
  }

  if (roles.includes('ROLE_CUSTOMER')) {
    return '/booking'
  }

  // 默认回退
  return '/booking'
}
