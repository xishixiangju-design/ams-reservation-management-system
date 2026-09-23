import request from '@/utils/request'

export interface LogQuery {
  pageNum: number
  pageSize: number
  module?: string
  username?: string
  status?: number
}

export interface LogDTO {
  id: number
  username: string
  module: string
  businessType: string
  method: string
  operParam: string
  ipAddr: string
  operTime: string
  status: number
  errorMsg?: string
  costTime?: number
}

export function getOperationLogs(params: LogQuery) {
  return request({
    url: '/api/admin/log/list',
    method: 'get',
    params
  })
}
