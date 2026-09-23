import request, { type ResponseResult } from '@/utils/request'

export interface WaitingListQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  status?: string
  startDate?: string
  endDate?: string
}

export interface WaitingListDTO {
  id: number
  storeId: number
  customerId: number
  customerName: string
  customerPhone: string
  serviceId: number
  serviceName: string
  techId: number
  techName: string
  expectedDate: string
  timeRange: string
  peopleCount: number
  status: string // WAITING, NOTIFIED, EXPIRED, CONVERTED
  createTime: string
  expiryTime: string
}

export function getAdminWaitingList(params: WaitingListQuery) {
  return request<any, ResponseResult<{ list: WaitingListDTO[]; total: number }>>({
    url: '/waiting-list/admin/list',
    method: 'get',
    params
  })
}

export function getMyWaitingList() {
  return request<any, ResponseResult<WaitingListDTO[]>>({
    url: '/waiting-list/me',
    method: 'get'
  })
}

export function convertWaitingList(id: number) {
  return request<any, ResponseResult<void>>({
    url: `/waiting-list/${id}/convert`,
    method: 'post'
  })
}
