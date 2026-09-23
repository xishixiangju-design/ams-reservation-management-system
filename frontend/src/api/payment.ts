import request from '@/utils/request'

export interface PaymentStatus {
  status: 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED'
  amount: number
  payTime: string
}

export function checkPaymentStatus(outTradeNo: string) {
  return request<PaymentStatus>({
    url: '/payment/status',
    method: 'get',
    params: { outTradeNo }
  })
}
