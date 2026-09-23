<template>
  <div class="payment-result">
    <div class="card result-card">
      <div v-if="status === 'success'" class="result-content">
        <el-icon class="icon success"><CircleCheckFilled /></el-icon>
        <h2>{{ $t('payment.success') }}</h2>
        <p>{{ $t('payment.successMsg') }}</p>
        <div v-if="resultData" class="details">
          <p>
            {{ $t('payment.amount') }}: <span class="price">¥{{ resultData.amount }}</span>
          </p>
          <p class="time">{{ $t('payment.time') }}: {{ formatTime(resultData.payTime) }}</p>
        </div>
        <div class="actions">
          <el-button type="primary" @click="goToMyAppointments">{{
            $t('payment.viewOrder')
          }}</el-button>
          <el-button @click="goHome">{{ $t('payment.backHome') }}</el-button>
        </div>
      </div>

      <div v-else-if="status === 'fail'" class="result-content">
        <el-icon class="icon fail"><CircleCloseFilled /></el-icon>
        <h2>{{ $t('payment.fail') }}</h2>
        <p>{{ $t('payment.failMsg') }}</p>
        <div class="actions">
          <el-button type="primary" @click="retryPayment">{{ $t('payment.retry') }}</el-button>
          <el-button @click="goHome">{{ $t('payment.backHome') }}</el-button>
        </div>
      </div>

      <div v-else class="result-content">
        <el-icon class="icon loading is-loading"><Loading /></el-icon>
        <h2>{{ $t('payment.loading') }}</h2>
        <p>{{ $t('payment.loadingMsg') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CircleCheckFilled, CircleCloseFilled, Loading } from '@element-plus/icons-vue'
import { checkPaymentStatus, type PaymentStatus } from '@/api/payment'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const status = ref<'loading' | 'success' | 'fail'>('loading')
const resultData = ref<PaymentStatus | null>(null)
const retryCount = ref(0)

const formatTime = (time: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const checkStatus = async () => {
  // Alipay returns params like: out_trade_no, trade_no, total_amount, etc.
  const outTradeNo = route.query.out_trade_no as string

  if (!outTradeNo) {
    status.value = 'fail'
    return
  }

  try {
    const res = await checkPaymentStatus(outTradeNo)
    if (res.data) {
      if (res.data.status === 'SUCCESS') {
        status.value = 'success'
        resultData.value = res.data
      } else if (res.data.status === 'PENDING') {
        // 如果状态仍为 PENDING，稍后重试（允许一定的回调延迟）
        if (retryCount.value < 3) {
          retryCount.value++
          setTimeout(checkStatus, 2000)
        } else {
          status.value = 'fail'
        }
      } else {
        status.value = 'fail'
      }
    }
  } catch (e) {
    console.error(e)
    // 网络错误也尝试重试几次
    if (retryCount.value < 3) {
      retryCount.value++
      setTimeout(checkStatus, 2000)
    } else {
      status.value = 'fail'
    }
  }
}

onMounted(() => {
  checkStatus()
})

const goToMyAppointments = () => {
  router.push({ name: 'my-appointments' })
}

const goHome = () => {
  router.push('/booking/service')
}

const retryPayment = () => {
  // Navigate back to my appointments to pay again (if we had a pay button there)
  // or just go home
  router.push({ name: 'my-appointments' })
}
</script>

<style scoped>
.payment-result {
  max-width: 600px;
  margin: 40px auto;
  padding: 20px;
}

.result-card {
  background: white;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.result-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.success {
  color: #67c23a;
}

.fail {
  color: #f56c6c;
}

.loading {
  color: #409eff;
}

h2 {
  margin-bottom: 10px;
  color: #303133;
}

p {
  color: #606266;
  margin-bottom: 20px;
}

.details {
  margin: 10px 0 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  width: 100%;
  box-sizing: border-box;
}

.details p {
  margin: 5px 0;
  color: #606266;
  font-size: 14px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.time {
  font-size: 12px;
  color: #909399;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}
</style>
