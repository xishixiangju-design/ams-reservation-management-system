import request from '@/utils/request'

export interface Notification {
  id: number
  title: string
  content: string
  createTime: string
  readStatus: number // 0: Unread, 1: Read
  type: string
}

export function getMyNotifications(params: any) {
  return request({
    url: '/notifications/me',
    method: 'get',
    params
  })
}

export function markAsRead(id: number) {
  return request({
    url: `/notifications/${id}/read`,
    method: 'put'
  })
}

export function markAllAsRead() {
  return request({
    url: '/notifications/read-all',
    method: 'put'
  })
}

export function deleteNotification(id: number) {
  return request({
    url: `/notifications/${id}`,
    method: 'delete'
  })
}

export function getUnreadCount() {
  return request({
    url: '/notifications/unread-count',
    method: 'get'
  })
}
