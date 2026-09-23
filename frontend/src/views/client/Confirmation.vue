<template>
  <div class="confirmation">
    <!-- Summary -->
    <div class="card summary-card">
      <h3>预约信息</h3>
      <div
        v-for="(service, index) in bookingStore.selectedServices"
        :key="service.id"
        class="info-row"
      >
        <span class="label">服务项目 {{ index + 1 }}</span>
        <span class="value">{{ service.name }}</span>
      </div>
      <div class="info-row">
        <span class="label">服务技师</span>
        <span class="value">
          {{ bookingStore.isAnyTech ? '任意技师' : bookingStore.selectedTech?.realName }}
        </span>
      </div>
      <div class="info-row">
        <span class="label">预约人数</span>
        <span class="value">{{ bookingStore.peopleCount }}人</span>
      </div>
      <div class="info-row">
        <span class="label">预约时间</span>
        <span class="value highlight">
          {{ bookingStore.selectedDate }} {{ bookingStore.selectedTime }}
        </span>
      </div>
      <div class="info-row">
        <span class="label">总时长</span>
        <span class="value">{{ bookingStore.totalDuration }}分钟</span>
      </div>
      <div class="info-row">
        <span class="label">原价</span>
        <span class="value price" :class="{ 'original-price': hasDiscount }"
          >¥{{ originalTotal.toFixed(2) }}</span
        >
      </div>
      <div v-if="hasDiscount" class="info-row">
        <span class="label">优惠价</span>
        <span class="value price member-price">¥{{ finalPrice }}</span>
      </div>
    </div>

    <!-- Customer Info Form -->
    <div class="card form-card">
      <h3>联系信息</h3>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="如有特殊需求请备注" />
        </el-form-item>
      </el-form>
    </div>

    <!-- Footer -->
    <div class="footer-action">
      <div class="price-bar">
        合计: <span class="final-price">¥{{ finalPrice }}</span>
      </div>
      <div class="buttons">
        <el-button @click="prevStep">上一步</el-button>
        <el-button type="primary" :loading="submitting" @click="submitBooking">
          立即预约
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useBookingStore } from '@/stores/booking'
import {
  createAppointment,
  type BookingRequest,
  joinWaitlist,
  initiatePayment
} from '@/api/appointment'
import { getMemberInfo } from '@/api/member'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const bookingStore = useBookingStore()
const formRef = ref<FormInstance>()
const { t } = useI18n()
const submitting = ref(false)
const discountRate = ref(1)

const hasDiscount = computed(() => Number(finalPrice.value) < originalTotal.value)

const form = reactive({
  name: '',
  phone: '',
  remark: ''
})

const rules = reactive<FormRules>({
  name: [{ required: true, message: t('common.pleaseInput'), trigger: 'blur' }],
  phone: [
    { required: true, message: t('common.pleaseInput'), trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: t('common.error'), trigger: 'blur' }
  ]
})

const finalPrice = computed(() => {
  return bookingStore.totalPrice.toFixed(2)
})

const originalTotal = computed(() => {
  const total =
    bookingStore.selectedServices.reduce((sum, s) => sum + s.price, 0) * bookingStore.peopleCount
  return Math.round(total * 100) / 100
})

const submitBooking = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitting.value = true
    try {
      // Construct ISO string for LocalDateTime: YYYY-MM-DDTHH:mm:ss
      const startTime = `${bookingStore.selectedDate}T${bookingStore.selectedTime}`

      const request: BookingRequest = {
        startTime,
        remark: `${form.remark} [Name:${form.name}, Phone:${form.phone}]`,
        peopleCount: bookingStore.peopleCount,
        items: bookingStore.selectedServices.map((s) => ({
          serviceId: s.id!,
          techId: bookingStore.selectedTech?.userId
        }))
      }

      // Save to store for Success page
      bookingStore.setCustomerInfo(form)

      const res = await createAppointment(request)
      const appointmentId = res.data

      // Show Payment Dialog or Redirect directly
      try {
        await ElMessageBox.confirm(
          t('appointment.create.success', { count: 1 }),
          t('common.confirm'),
          {
            confirmButtonText: t('appointment.create.payNow'),
            cancelButtonText: t('appointment.create.payLater'),
            type: 'success',
            closeOnClickModal: false,
            closeOnPressEscape: false
          }
        )

        // Initiate Payment
        try {
          const res = await initiatePayment(appointmentId)
          const htmlForm = res.data
          // Create a temporary div to render the form and submit it
          const div = document.createElement('div')
          div.innerHTML = htmlForm
          document.body.appendChild(div)
          const form = div.querySelector('form')
          if (form) {
            form.submit()
          }
        } catch (e) {
          ElMessage.error(t('payment.fail'))
          router.push({ name: 'member-center' })
        }
      } catch (cancel) {
        // User clicked "Pay Later" or closed dialog
        ElMessage.info(t('common.success'))
        router.push({ name: 'member-center' })
      }
    } catch (error: any) {
      // Handle "No Resource" error specifically
      if (
        error.response?.data?.message?.includes('无可用') ||
        error.response?.data?.message?.includes('占用') ||
        error.message?.includes('无可用') ||
        error.message?.includes('占用')
      ) {
        try {
          await ElMessageBox.confirm(t('booking.waitlistConfirm'), t('booking.joinWaitlist'), {
            confirmButtonText: t('booking.joinWaitlist'),
            cancelButtonText: t('common.cancel'),
            type: 'warning'
          })

          try {
            // Calculate time range
            const duration = bookingStore.totalDuration || 60
            const startStr = bookingStore.selectedTime

            const addMinutes = (time: string, mins: number) => {
              const [h, m] = time.split(':').map(Number)
              const date = new Date()
              date.setHours(h, m + mins)
              return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
            }
            const endStr = addMinutes(startStr!, duration)
            const timeRange = `${startStr}-${endStr}`

            await joinWaitlist({
              serviceId: bookingStore.selectedServices[0].id!,
              techId: bookingStore.selectedTech?.userId,
              expectedDate: bookingStore.selectedDate!,
              timeRange: timeRange,
              peopleCount: bookingStore.peopleCount
            })
            ElMessage.success(t('booking.waitlistSuccess'))
            router.push({ name: 'member-center' })
          } catch (e) {
            // Waitlist join failed
          }
        } catch (cancel) {
          // User cancelled waitlist join
        }
      }
    } finally {
      submitting.value = false
    }
  } catch (validationError) {
    // Form validation failed
  }
}

const prevStep = () => {
  router.push({ name: 'booking-tech' })
}

onMounted(async () => {
  if (!bookingStore.selectedTime) {
    router.replace({ name: 'booking-tech' })
    return
  }
  // Fetch discount (Optional, just for info)
  try {
    const res = await getMemberInfo()
    if (res.data && res.data.discountRate) {
      discountRate.value = res.data.discountRate
    }
  } catch (e) {
    // 忽略会员信息拉取失败：不影响下单流程，保持默认折扣
    console.warn('获取会员折扣失败，使用默认价', e)
  }
})
</script>

<style scoped>
.card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
}

.card h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}

.label {
  color: #909399;
}

.value {
  font-weight: 500;
}

.highlight {
  color: #409eff;
  font-weight: bold;
}

.price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.original-price {
  text-decoration: line-through;
  color: #909399;
  font-size: 14px;
  margin-right: 5px;
}

.member-price {
  color: #f56c6c;
  font-size: 18px;
}

.footer-action {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-bar {
  font-size: 14px;
}

.final-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.buttons {
  display: flex;
  gap: 10px;
}
</style>
