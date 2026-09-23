import request, { type ResponseResult } from '@/utils/request'

export interface SlotQueryRequest {
  date: string // YYYY-MM-DD
  techId?: number
  serviceId?: number
  serviceIds?: number[]
  peopleCount?: number
}

export interface TimeSlotDTO {
  startTime: string // HH:mm:ss
  endTime: string // HH:mm:ss
  available: boolean
  availableTechCount: number
}

export interface BookingItemDTO {
  serviceId: number
  techId?: number
  roomId?: number
}

export interface BookingRequest {
  customerId?: number
  startTime: string // ISO 8601
  remark?: string
  contactName?: string
  contactPhone?: string
  items: BookingItemDTO[]
  peopleCount?: number
}

export interface AppointmentQuery {
  pageNum: number
  pageSize: number
  startDate?: string
  endDate?: string
  status?: number
  keyword?: string
}

export interface AppointmentDTO {
  id: number
  customerId: number
  storeId?: number
  customer?: {
    username: string
    nickname: string
  }
  contactName?: string
  contactPhone?: string
  startTime: string
  endTime: string
  status: number
  paymentStatus?: string // UNPAID, PAID, REFUNDED
  refundStatus?: number // 0-None, 1-Processing, 2-Completed, 3-Failed
  totalAmount: number
  remark?: string
  createTime: string
  peopleCount?: number
  items?: any[]
  verificationCode?: string
}

export const getAvailableSlots = (data: SlotQueryRequest) => {
  return request<any, TimeSlotDTO[]>({
    url: '/appointments/slots',
    method: 'post',
    data
  })
}

export interface BatchBookingRequest {
  customerId?: number
  contactName?: string
  contactPhone?: string
  remark?: string
  serviceIds: number[]
  startTimes: string[] // ISO 8601 LocalDateTime
  techId?: number
  storeId?: number
}

export const batchCreateAppointment = (data: BatchBookingRequest) => {
  return request<any, number[]>({
    url: '/appointments/admin/batch',
    method: 'post',
    data
  })
}

export const batchPayAppointment = (appointmentIds: number[]) => {
  return request<any, void>({
    url: '/appointments/admin/batch/pay',
    method: 'post',
    data: appointmentIds
  })
}

export const createAppointment = (data: BookingRequest) => {
  return request<any, ResponseResult<number>>({
    url: '/appointments',
    method: 'post',
    data
  })
}

export const createAppointmentByAdmin = (data: BookingRequest) => {
  return request<any, ResponseResult<number>>({
    url: '/api/admin/appointment/create',
    method: 'post',
    data
  })
}

export const markAsPaid = (id: number) => {
  return request<any, void>({
    url: `/api/admin/appointment/${id}/pay`,
    method: 'post'
  })
}

export const revokePayment = (id: number, reason: string) => {
  return request<any, void>({
    url: `/api/admin/appointment/${id}/revoke-pay`,
    method: 'post',
    params: { reason }
  })
}

export const completeAppointmentByAdmin = (id: number) => {
  return request<any, void>({
    url: `/api/admin/appointment/${id}/complete`,
    method: 'post'
  })
}

export const getAppointmentById = (id: number) => {
  return request<any, AppointmentDTO>({
    url: `/appointments/${id}`,
    method: 'get'
  })
}

export const getMyAppointments = (params?: AppointmentQuery) => {
  return request<any, ResponseResult<{ list: AppointmentDTO[]; total: number }>>({
    url: '/appointments/me',
    method: 'get',
    params
  })
}

export const cancelAppointment = (id: number) => {
  return request<any, void>({
    url: `/appointments/${id}/cancel`,
    method: 'post'
  })
}

export const getAdminAppointmentList = (params: AppointmentQuery, config?: any) => {
  return request<any, ResponseResult<{ list: AppointmentDTO[]; total: number }>>({
    url: '/appointments/admin/list',
    method: 'get',
    params,
    ...config
  })
}

export const completeAppointment = (id: number) => {
  return request<any, void>({
    url: `/appointments/${id}/complete`,
    method: 'post'
  })
}

export const getCustomerAppointments = (customerId: number) => {
  return request<any, AppointmentDTO[]>({
    url: `/appointments/admin/customer/${customerId}`,
    method: 'get'
  })
}

export interface JoinWaitlistDTO {
  serviceId: number
  techId?: number
  expectedDate: string // YYYY-MM-DD
  timeRange: string // HH:mm-HH:mm
  peopleCount?: number
}

export const joinWaitlist = (data: JoinWaitlistDTO) => {
  return request<any, void>({
    url: '/waiting-list/join',
    method: 'post',
    data
  })
}

export const rescheduleAppointment = (
  id: number,
  data: { newStartTime: string; newTechId?: number }
) => {
  return request<any, void>({
    url: `/appointments/${id}/reschedule`,
    method: 'post',
    data
  })
}

export const initiatePayment = (appointmentId: number) => {
  return request<any, ResponseResult<string>>({
    url: '/payment/alipay/pay',
    method: 'post',
    params: { appointmentId }
  })
}
