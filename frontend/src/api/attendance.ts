import request from '@/utils/request'

export interface LeaveApplicationDTO {
  storeId: number
  type: string
  startTime: string
  endTime: string
  reason: string
}

export interface LeaveApprovalDTO {
  id: number
  status: string
}

export interface LeaveQueryDTO {
  storeId?: number
  techId?: number
  techName?: string
  type?: string
  status?: string
  startTime?: string
  endTime?: string
}

export interface ClockInDTO {
  storeId: number
  type: string
  location?: string
  time?: string
}

export interface AttendanceQueryDTO {
  storeId?: number
  techId?: number
  techName?: string
  type?: string
  status?: string
  startTime?: string
  endTime?: string
}

export interface LeaveRequest {
  id: number
  storeId: number
  techId: number
  techName?: string
  type: string
  startTime: string
  endTime: string
  reason: string
  status: string
  auditBy?: number
  auditTime?: string
  createTime: string
  updateTime: string
}

export interface AttendanceRecord {
  id: number
  storeId: number
  techId: number
  techName?: string
  type: string
  time: string
  location?: string
  status?: string
  createTime: string
}

// Leave APIs
export function applyLeave(data: LeaveApplicationDTO) {
  return request({
    url: '/attendance/leave/apply',
    method: 'post',
    data
  })
}

export function approveLeave(data: LeaveApprovalDTO) {
  return request({
    url: '/attendance/leave/approve',
    method: 'post',
    data
  })
}

export function getLeaveList(params: LeaveQueryDTO) {
  return request<any, LeaveRequest[]>({
    url: '/attendance/leave/list',
    method: 'get',
    params
  })
}

export interface AttendanceUpdateDTO {
  id: number
  status: string
}

// Attendance APIs
export function clockIn(data: ClockInDTO) {
  return request({
    url: '/attendance/clock-in',
    method: 'post',
    data
  })
}

export function updateAttendance(data: AttendanceUpdateDTO) {
  return request({
    url: '/attendance/record/update',
    method: 'post',
    data
  })
}

export function getAttendanceList(params: AttendanceQueryDTO) {
  return request<any, AttendanceRecord[]>({
    url: '/attendance/record/list',
    method: 'get',
    params
  })
}
