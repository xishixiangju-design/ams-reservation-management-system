import request from '@/utils/request'

export interface CustomerDTO {
  id: number
  username: string // phone
  nickname: string
  avatar?: string
  createTime: string
  status?: number
  membershipLevel?: string
  discountRate?: number
  violationCount?: number
  password?: string
  email?: string
}

export const addCustomer = (data: Partial<CustomerDTO>) => {
  return request<any, void>({
    url: '/customers',
    method: 'post',
    data
  })
}

export const searchCustomers = (keyword: string) => {
  return request<any, CustomerDTO[]>({
    url: '/customers/search',
    method: 'get',
    params: { keyword }
  })
}

export const getCustomerList = (params: {
  pageNum: number
  pageSize: number
  keyword?: string
}) => {
  return request<any, { list: CustomerDTO[]; total: number }>({
    url: '/customers',
    method: 'get',
    params
  })
}

export const deleteCustomer = (id: number) => {
  return request<any, void>({
    url: `/customers/${id}`,
    method: 'delete'
  })
}

export const updateCustomer = (id: number, data: Partial<CustomerDTO>) => {
  return request<any, void>({
    url: `/customers/${id}`,
    method: 'put',
    data
  })
}
