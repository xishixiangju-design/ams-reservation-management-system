import request from '@/utils/request'

export interface RoomDTO {
  id?: number
  name: string
  type: string // SINGLE, DOUBLE
  capacity?: number
  status?: number // 1: IDLE, 2: IN_USE, 3: MAINTENANCE
  storeId?: number
}

export const getRoomList = () => {
  return request<any, RoomDTO[]>({
    url: '/rooms',
    method: 'get'
  })
}

export const createRoom = (data: RoomDTO) => {
  return request<any, void>({
    url: '/rooms',
    method: 'post',
    data
  })
}

export const updateRoom = (id: number, data: RoomDTO) => {
  return request<any, void>({
    url: `/rooms/${id}`,
    method: 'put',
    data
  })
}

export const deleteRoom = (id: number) => {
  return request<any, void>({
    url: `/rooms/${id}`,
    method: 'delete'
  })
}

export const getRoomById = (id: number) => {
  return request<any, RoomDTO>({
    url: `/rooms/${id}`,
    method: 'get'
  })
}
