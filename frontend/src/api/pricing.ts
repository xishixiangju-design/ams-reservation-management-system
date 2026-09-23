import request from '@/utils/request'

export interface PricingRequest {
  serviceId: number
  customerId?: number
  peopleCount: number
}

export interface PricingResult {
  code: number
  message: string
  data: number // BigDecimal -> number
}

export function calculatePrice(data: PricingRequest) {
  return request<PricingResult>({
    url: '/api/pricing/calculate',
    method: 'post',
    data
  })
}
