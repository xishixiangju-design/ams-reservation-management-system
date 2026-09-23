import request from '@/utils/request'
import type { ResponseResult } from '@/utils/request'
import type { AppointmentQuery, AppointmentDTO } from './appointment'

export interface TechnicianDTO {
  userId?: number
  realName?: string
  username?: string
  password?: string
  nickname?: string
  avatar?: string
  userStatus?: number
  status?: string // IDLE, BUSY, LEAVE
  level?: string
  introCn?: string
  introJp?: string
  sysUser?: {
    username: string
    nickname?: string
    avatar?: string
    status?: number
  }
}

export interface TechnicianStatsDTO {
  userId: number
  technicianName: string
  activeOrderCount: number
  totalOrderCount: number
}

export const getTechnicianList = () => {
  return request<any, ResponseResult<TechnicianDTO[]>>({
    url: '/technicians',
    method: 'get'
  })
}

export const createTechnician = (data: TechnicianDTO) => {
  return request<any, ResponseResult<void>>({
    url: '/technicians',
    method: 'post',
    data
  })
}

export const updateTechnician = (id: number, data: TechnicianDTO) => {
  return request<any, ResponseResult<void>>({
    url: `/technicians/${id}`,
    method: 'put',
    data
  })
}

export const deleteTechnician = (id: number) => {
  return request<any, ResponseResult<void>>({
    url: `/technicians/${id}`,
    method: 'delete'
  })
}

export const getTechnicianStats = (id: number) => {
  return request<any, ResponseResult<TechnicianStatsDTO>>({
    url: `/technicians/${id}/stats`,
    method: 'get'
  })
}

export const updateTechnicianStatus = (id: number, status: string) => {
  return request<any, ResponseResult<void>>({
    url: `/technicians/${id}/status`,
    method: 'patch',
    params: { status }
  })
}

export interface LeaveRequest {
  id?: number
  techId?: number
  type: string
  startTime: string
  endTime: string
  reason: string
  status?: string // PENDING, APPROVED, REJECTED
  createTime?: string
  techName?: string
}

export interface Attendance {
  id?: number
  techId?: number
  type: string // CLOCK_IN, CLOCK_OUT
  time: string
  createTime?: string
  techName?: string
}

export const getTechnicianAppointments = (params: AppointmentQuery) => {
  return request<any, ResponseResult<{ list: AppointmentDTO[]; total: number }>>({
    url: '/appointments/technician/me',
    method: 'get',
    params
  })
}

export const createLeaveRequest = (data: LeaveRequest) => {
  return request<any, ResponseResult<void>>({
    url: '/leave/create',
    method: 'post',
    data
  })
}

export const getMyLeaveRequests = () => {
  return request<any, ResponseResult<LeaveRequest[]>>({
    url: '/leave/my',
    method: 'get'
  })
}

export const clockIn = () => {
  return request<any, ResponseResult<void>>({
    url: '/attendance/clock-in',
    method: 'post'
  })
}

export const clockOut = () => {
  return request<any, ResponseResult<void>>({
    url: '/attendance/clock-out',
    method: 'post'
  })
}

export const getMyAttendance = (date?: string) => {
  return request<any, ResponseResult<Attendance[]>>({
    url: '/attendance/my',
    method: 'get',
    params: { date }
  })
}
