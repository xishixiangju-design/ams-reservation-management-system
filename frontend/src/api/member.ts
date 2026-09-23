import request from '@/utils/request'
import type { ResponseResult } from '@/utils/request'

export interface MemberInfo {
  totalConsumption: number
  currentLevel: string
  discountRate: number
  levelName: string
  iconUrl?: string
  nextLevelName?: string
  nextLevelThreshold?: number
  needAmount?: number
  progress?: number
  isMax?: boolean
}

export interface MemberLevelRule {
  id: number
  name: string
  code: string
  minConsumption: number
  discountRate: number
  iconUrl?: string
  rightsDesc?: string
}

export const getMemberInfo = () => {
  return request<any, ResponseResult<MemberInfo>>({
    url: '/member/info',
    method: 'get'
  })
}

export const getLevelRules = () => {
  return request<any, ResponseResult<MemberLevelRule[]>>({
    url: '/member/rules',
    method: 'get'
  })
}
