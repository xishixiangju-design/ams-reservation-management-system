<template>
  <div class="success-page">
    <div class="icon-wrapper">
      <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
    </div>
    <h2>预约成功!</h2>
    <p class="sub-title">请出示下方二维码核销</p>

    <div class="qr-placeholder">
      <div class="qr-code">
        <img v-if="qrCodeUrl" :src="qrCodeUrl" alt="QR Code" />
        <div v-else class="qr-content">Generating...</div>
      </div>
      <p class="code-text">核销码: {{ mockCode }}</p>
    </div>

    <div class="info-card">
      <div class="row">
        <span>预约时间</span>
        <strong>{{ bookingStore.selectedDate }} {{ bookingStore.selectedTime }}</strong>
      </div>
      <div class="row">
        <span>服务项目</span>
        <strong>{{ bookingStore.selectedServices.map((s) => s.name).join(', ') }}</strong>
      </div>
      <div class="row">
        <span>联系人</span>
        <strong>{{ bookingStore.customerInfo.name }}</strong>
      </div>
    </div>

    <div class="actions">
      <el-button @click="viewOrder">查看订单详情</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useBookingStore } from '@/stores/booking'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'

const router = useRouter()
const bookingStore = useBookingStore()

const mockCode = ref(Math.random().toString(36).substring(2, 8).toUpperCase())
const qrCodeUrl = ref('')

const generateQRCode = async () => {
  try {
    qrCodeUrl.value = await QRCode.toDataURL(mockCode.value, { width: 200, margin: 1 })
  } catch (err) {
    console.error('QR Code generation failed', err)
  }
}

onMounted(() => {
  generateQRCode()
})

const viewOrder = () => {
  // Navigate to order detail (not implemented yet, stay here or go home)
  ElMessage.info('common.loading')
}

// Clean up store when leaving (optional, maybe keep it until new booking started)
// But usually good to reset if leaving the flow.
// onUnmounted(() => {
//   bookingStore.reset()
// })
</script>

<style scoped>
.success-page {
  text-align: center;
  padding: 40px 20px;
}

.icon-wrapper {
  margin-bottom: 20px;
}

.success-icon {
  font-size: 64px;
  color: #67c23a;
}

.sub-title {
  color: #909399;
  margin-bottom: 30px;
}

.qr-placeholder {
  margin: 30px auto;
  width: 200px;
}

.qr-code {
  width: 200px;
  height: 200px;
  background-color: #f2f6fc;
  border: 1px dashed #c0c4cc;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.code-text {
  font-family: monospace;
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 2px;
}

.info-card {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
  text-align: left;
}

.row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}
</style>
