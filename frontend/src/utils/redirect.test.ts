import { describe, it, expect } from 'vitest'
import { getRedirectPath } from './redirect'

describe('getRedirectPath', () => {
  it('should redirect admin to dashboard', () => {
    expect(getRedirectPath(['ROLE_ADMIN'])).toBe('/dashboard')
    expect(getRedirectPath(['ROLE_MANAGER', 'ROLE_CUSTOMER'])).toBe('/dashboard')
  })

  it('should redirect tech to appointment list', () => {
    expect(getRedirectPath(['ROLE_TECH'])).toBe('/admin/technician/my-appointments')
  })

  it('should redirect customer to booking', () => {
    expect(getRedirectPath(['ROLE_CUSTOMER'])).toBe('/booking')
  })

  it('should handle empty roles', () => {
    expect(getRedirectPath([])).toBe('/booking')
  })

  it('should handle unknown roles', () => {
    expect(getRedirectPath(['ROLE_UNKNOWN'])).toBe('/booking')
  })
})
