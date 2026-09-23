import request, { type ResponseResult } from '@/utils/request'

export interface DashboardSummary {
  todayAppointments: number
  todayRevenue: number
  waitlistCount: number
  violationCount: number
  pendingServicesCount: number
}

export interface RoomStatus {
  id: number
  name: string
  type: string
  status: 'IDLE' | 'OCCUPIED' | 'CLEANING'
  currentCustomer?: string
  remainingTime?: string
}

export interface TechStatus {
  id: number
  name: string
  level: string
  status: 'IDLE' | 'BUSY' | 'LEAVE'
  wheelSeq: number
  currentTask?: string
}

export function getSummary() {
  return request<any, ResponseResult<DashboardSummary>>({
    url: '/dashboard/summary',
    method: 'get'
  })
}

export function getRoomStatus() {
  return request<any, ResponseResult<RoomStatus[]>>({
    url: '/dashboard/room-status',
    method: 'get'
  })
}

export function getTechStatus() {
  return request<any, ResponseResult<TechStatus[]>>({
    url: '/dashboard/tech-status',
    method: 'get'
  })
}

export function getCalendarStats(startDate: string, endDate: string) {
  return request<any, ResponseResult<Array<{ date: string; count: number }>>>({
    url: '/dashboard/calendar-stats',
    method: 'get',
    params: { startDate, endDate }
  })
}
