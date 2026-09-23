import request from '@/utils/request'

export interface ServiceDTO {
  id?: number
  name: string
  description: string
  price: number
  duration: number
  status?: number
  storeId?: number
  category?: string
  scenario?: string
  recommendationScore?: number
  mutexGroup?: string
  createTime?: string
  updateTime?: string
}

export const getServiceList = () => {
  return request<any, ServiceDTO[]>({
    url: '/services',
    method: 'get'
  })
}

export const createService = (data: ServiceDTO) => {
  return request<any, void>({
    url: '/services',
    method: 'post',
    data
  })
}

export const updateService = (id: number, data: ServiceDTO) => {
  return request<any, void>({
    url: `/services/${id}`,
    method: 'put',
    data
  })
}

export const deleteService = (id: number) => {
  return request<any, void>({
    url: `/services/${id}`,
    method: 'delete'
  })
}

export const getServiceById = (id: number) => {
  return request<any, ServiceDTO>({
    url: `/services/${id}`,
    method: 'get'
  })
}
