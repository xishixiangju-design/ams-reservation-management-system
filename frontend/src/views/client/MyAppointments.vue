<template>
  <div class="my-appointments-container">
    <div class="header">
      <h2>{{ $t('myAppointments.title') }}</h2>
    </div>

    <el-tabs v-model="activeTab" class="appointment-tabs">
      <el-tab-pane :label="$t('myAppointments.tabs.official')" name="appointment">
        <div class="filters">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            :range-separator="$t('common.to')"
            :start-placeholder="$t('common.startTime')"
            :end-placeholder="$t('common.endTime')"
            value-format="YYYY-MM-DD"
            :shortcuts="shortcuts"
            style="width: 300px; margin-right: 16px"
            @change="handleFilterChange"
          />
          <el-select
            v-model="status"
            :placeholder="$t('common.status')"
            clearable
            style="width: 150px"
            @change="handleFilterChange"
          >
            <el-option :label="$t('myAppointments.status.unpaid')" :value="1" />
            <el-option :label="$t('myAppointments.status.completed')" :value="2" />
            <el-option :label="$t('myAppointments.status.cancelled')" :value="3" />
            <el-option :label="$t('myAppointments.status.violation')" :value="4" />
            <el-option :label="$t('myAppointments.status.pending')" :value="5" />
          </el-select>
        </div>

        <div class="appointment-list">
          <el-skeleton :loading="loading" animated :count="3">
            <template #template>
              <el-card style="margin-bottom: 16px">
                <div style="display: flex; justify-content: space-between; margin-bottom: 10px">
                  <el-skeleton-item variant="text" style="width: 30%" />
                  <el-skeleton-item variant="text" style="width: 10%" />
                </div>
                <el-skeleton-item variant="p" style="width: 80%" />
                <el-skeleton-item variant="p" style="width: 60%" />
              </el-card>
            </template>
            <template #default>
              <el-empty v-if="!loading && total === 0" :description="$t('common.noData')" />

              <el-card
                v-for="item in appointmentList"
                :key="item.id"
                class="appointment-card"
                shadow="hover"
              >
                <template #header>
                  <div class="card-header">
                    <span class="time-info">
                      <el-icon><Calendar /></el-icon>
                      {{ formatDateTime(item.startTime) }}
                    </span>
                    <el-tag :type="getStatusType(item.status)">{{
                      getStatusText(item.status)
                    }}</el-tag>
                  </div>
                </template>

                <div class="card-body">
                  <div v-if="item.refundStatus && item.refundStatus > 0" class="info-row">
                    <span class="label">{{ $t('myAppointments.labels.refund') }}</span>
                    <el-tag :type="getRefundStatusType(item.refundStatus)" size="small">
                      {{ getRefundStatusText(item.refundStatus) }}
                    </el-tag>
                  </div>
                  <div v-if="item.items && item.items.length > 0" class="info-row">
                    <span class="label">{{ $t('myAppointments.labels.service') }}</span>
                    <div class="value-list">
                      <div v-for="sub in item.items" :key="sub.id" class="service-item">
                        {{ sub.service?.name || $t('common.unknown') }} ({{ sub.service?.duration }}
                        min)
                      </div>
                    </div>
                  </div>
                  <div class="info-row">
                    <span class="label">{{ $t('myAppointments.labels.id') }}</span>
                    <span class="value">#{{ item.id }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">{{ $t('myAppointments.labels.people') }}</span>
                    <span class="value"
                      >{{ item.peopleCount }} {{ $t('common.people') || '人' }}</span
                    >
                  </div>
                  <div class="info-row">
                    <span class="label">{{ $t('myAppointments.labels.contact') }}</span>
                    <span class="value">{{ item.contactName }} ({{ item.contactPhone }})</span>
                  </div>
                  <div v-if="item.remark" class="info-row">
                    <span class="label">{{ $t('myAppointments.labels.remark') }}</span>
                    <span class="value">{{ item.remark }}</span>
                  </div>
                  <div class="price-row">
                    <span class="label">{{ $t('myAppointments.labels.total') }}</span>
                    <span class="price">¥{{ item.totalAmount }}</span>
                  </div>
                </div>

                <div class="card-footer">
                  <el-button
                    v-if="canPay(item)"
                    type="success"
                    size="small"
                    style="margin-right: 8px"
                    @click="handlePay(item)"
                  >
                    {{ $t('myAppointments.actions.pay') }}
                  </el-button>
                  <el-button
                    v-if="item.status === 5"
                    type="primary"
                    size="small"
                    style="margin-right: 8px"
                    @click="handleShowCode(item)"
                  >
                    核销码
                  </el-button>
                  <el-button
                    v-if="canCancel(item)"
                    type="primary"
                    size="small"
                    plain
                    style="margin-right: 8px"
                    @click="handleReschedule(item)"
                  >
                    {{ $t('myAppointments.actions.reschedule') }}
                  </el-button>
                  <el-button
                    v-if="canCancel(item)"
                    type="danger"
                    size="small"
                    plain
                    @click="handleCancel(item)"
                  >
                    {{ $t('myAppointments.actions.cancel') }}
                  </el-button>
                </div>
              </el-card>
            </template>
          </el-skeleton>
        </div>

        <div v-if="total > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20]"
            layout="total, sizes, prev, pager, next"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane :label="$t('myAppointments.tabs.waiting')" name="waiting">
        <div class="appointment-list">
          <el-skeleton :loading="waitingLoading" animated :count="3">
            <template #template>
              <el-card style="margin-bottom: 16px">
                <div style="display: flex; justify-content: space-between; margin-bottom: 10px">
                  <el-skeleton-item variant="text" style="width: 30%" />
                  <el-skeleton-item variant="text" style="width: 10%" />
                </div>
                <el-skeleton-item variant="p" style="width: 80%" />
              </el-card>
            </template>
            <template #default>
              <el-empty
                v-if="!waitingLoading && waitingList.length === 0"
                :description="$t('common.noData')"
              />

              <el-card
                v-for="item in waitingList"
                :key="item.id"
                class="appointment-card"
                shadow="hover"
              >
                <template #header>
                  <div class="card-header">
                    <span class="time-info">
                      <el-icon><Calendar /></el-icon>
                      {{ item.expectedDate }}
                    </span>
                    <el-tag :type="getWaitingStatusType(item.status)">{{
                      getWaitingStatusText(item.status)
                    }}</el-tag>
                  </div>
                </template>

                <div class="card-body">
                  <div class="info-row">
                    <span class="label">{{ $t('myAppointments.labels.expectedService') }}</span>
                    <span class="value">{{ item.serviceName || $t('common.unknown') }}</span>
                  </div>
                  <div v-if="item.techName" class="info-row">
                    <span class="label">{{ $t('myAppointments.labels.tech') }}</span>
                    <span class="value">{{ item.techName }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">{{ $t('myAppointments.labels.time') }}</span>
                    <span class="value">{{ item.timeRange }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">{{ $t('myAppointments.labels.people') }}</span>
                    <span class="value"
                      >{{ item.peopleCount }} {{ $t('common.people') || '人' }}</span
                    >
                  </div>
                  <div class="info-row">
                    <span class="label">{{ $t('myAppointments.labels.applyTime') }}</span>
                    <span class="value">{{ formatDateTime(item.createTime) }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">{{ $t('myAppointments.labels.expireTime') }}</span>
                    <span class="value">{{ formatDateTime(item.expiryTime) }}</span>
                  </div>
                </div>

                <div
                  v-if="item.status === 'WAITING' || item.status === 'NOTIFIED'"
                  class="card-footer"
                >
                  <el-button type="success" size="small" @click="handleConvert(item)">
                    {{ $t('myAppointments.actions.convertToBooking') }}
                  </el-button>
                </div>
              </el-card>
            </template>
          </el-skeleton>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Reschedule Dialog -->
    <el-dialog
      v-model="rescheduleDialogVisible"
      :title="$t('myAppointments.reschedule.title')"
      width="500px"
    >
      <el-form :model="rescheduleForm" label-width="100px">
        <el-form-item :label="$t('myAppointments.reschedule.newTime')" required>
          <el-date-picker
            v-model="rescheduleForm.newStartTime"
            type="datetime"
            :placeholder="$t('common.pleaseSelect')"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="(date: Date) => dayjs(date).isBefore(dayjs(), 'day')"
          />
        </el-form-item>
        <el-form-item :label="$t('myAppointments.reschedule.changeTech')">
          <el-select
            v-model="rescheduleForm.newTechId"
            :placeholder="$t('myAppointments.reschedule.keepTech')"
            clearable
          >
            <el-option
              v-for="tech in techList"
              :key="tech.userId"
              :label="tech.realName"
              :value="tech.userId || 0"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rescheduleDialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="submitReschedule">{{
            $t('myAppointments.reschedule.confirm')
          }}</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Verification Code Dialog -->
    <el-dialog
      v-model="verificationDialogVisible"
      title="核销码"
      width="300px"
      center
      append-to-body
    >
      <div style="text-align: center">
        <img
          v-if="verificationCodeUrl"
          :src="verificationCodeUrl"
          alt="QR Code"
          style="width: 200px; height: 200px"
        />
        <div style="margin-top: 10px; font-size: 18px; font-weight: bold; letter-spacing: 2px">
          {{ currentVerificationCode }}
        </div>
        <p style="color: #909399; font-size: 12px; margin-top: 5px">请出示此码给店员核销</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="verificationDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { Calendar } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import {
  getMyAppointments,
  cancelAppointment,
  rescheduleAppointment,
  initiatePayment,
  type AppointmentDTO
} from '@/api/appointment'
import { getTechnicianList, type TechnicianDTO } from '@/api/technician'
import { getMyWaitingList, convertWaitingList, type WaitingListDTO } from '@/api/waitingList'
import { useI18n } from 'vue-i18n'
import QRCode from 'qrcode'

const { t } = useI18n()
const activeTab = ref('appointment')
const loading = ref(false)
const appointmentList = ref<AppointmentDTO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// Verification Code
const verificationDialogVisible = ref(false)
const verificationCodeUrl = ref('')
const currentVerificationCode = ref('')

const handleShowCode = async (item: AppointmentDTO) => {
  currentVerificationCode.value =
    item.verificationCode ||
    `V${String(item.storeId || 0).padStart(3, '0')}${String(item.id).padStart(8, '0')}`
  const qrContent = JSON.stringify({
    appointmentId: item.id,
    userId: item.customerId,
    storeId: item.storeId
  })
  try {
    verificationCodeUrl.value = await QRCode.toDataURL(qrContent, { width: 200, margin: 1 })
    verificationDialogVisible.value = true
  } catch (err) {
    console.error(err)
    ElMessage.error('无法生成核销码')
  }
}

// Waiting List
const waitingLoading = ref(false)
const waitingList = ref<WaitingListDTO[]>([])

// Reschedule
const rescheduleDialogVisible = ref(false)
const rescheduleForm = reactive({
  id: 0,
  newStartTime: '',
  newTechId: undefined as number | undefined
})
const techList = ref<TechnicianDTO[]>([])

// Filters
const dateRange = ref<[string, string] | null>(null) // Default to all time
const status = ref<number | undefined>(undefined)

const shortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '未来一周',
    value: () => {
      const start = new Date()
      const end = new Date()
      end.setTime(start.getTime() + 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

const getStatusType = (status: number): 'success' | 'info' | 'warning' | 'danger' => {
  const map: Record<number, 'success' | 'info' | 'warning' | 'danger'> = {
    1: 'warning', // 待支付
    2: 'info', // 已完成
    3: 'danger', // 已取消
    4: 'danger', // 违约取消
    5: 'success' // 待消费
  }
  return map[status] || 'info'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    1: t('myAppointments.status.unpaid'),
    2: t('myAppointments.status.completed'),
    3: t('myAppointments.status.cancelled'),
    4: t('myAppointments.status.violation'),
    5: t('myAppointments.status.pending')
  }
  return map[status] || t('common.unknown')
}

const getWaitingStatusType = (
  status: string
): 'success' | 'info' | 'warning' | 'danger' | 'primary' => {
  const map: Record<string, 'success' | 'info' | 'warning' | 'danger' | 'primary'> = {
    WAITING: 'warning',
    NOTIFIED: 'success',
    CONVERTED: 'primary',
    EXPIRED: 'info'
  }
  return map[status] || 'info'
}

const getWaitingStatusText = (status: string) => {
  const map: Record<string, string> = {
    WAITING: t('myAppointments.waitingStatus.waiting'),
    NOTIFIED: t('myAppointments.waitingStatus.notified'),
    CONVERTED: t('myAppointments.waitingStatus.converted'),
    EXPIRED: t('myAppointments.waitingStatus.expired')
  }
  return map[status] || status
}

const getRefundStatusType = (
  status: number
): 'success' | 'info' | 'warning' | 'danger' | 'primary' => {
  const map: Record<number, 'success' | 'info' | 'warning' | 'danger' | 'primary'> = {
    0: 'info',
    1: 'warning', // Processing
    2: 'success', // Completed
    3: 'danger' // Failed
  }
  return map[status] || 'info'
}

const getRefundStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: t('myAppointments.refundStatus.none'),
    1: t('myAppointments.refundStatus.processing'),
    2: t('myAppointments.refundStatus.completed'),
    3: t('myAppointments.refundStatus.failed')
  }
  return map[status] || t('common.unknown')
}

const formatDateTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const canCancel = (item: AppointmentDTO) => {
  return item.status === 1 || item.status === 5 // Only Pending Payment or Pending Consumption
}

const canPay = (item: AppointmentDTO) => {
  // Only allow payment for Pending Payment status (1)
  return item.status === 1
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      startDate: dateRange.value ? dateRange.value[0] : undefined,
      endDate: dateRange.value ? dateRange.value[1] : undefined,
      status: status.value
    }
    const res = await getMyAppointments(params)

    // Handle standard ResponseResult structure { code: 200, data: { list: [], total: 0 } }
    if (res && res.data && Array.isArray(res.data.list)) {
      appointmentList.value = res.data.list
      total.value = res.data.total
    }
    // Handle unwrapped structure (if interceptor changes) { list: [], total: 0 }
    else if (res && Array.isArray((res as any).list)) {
      appointmentList.value = (res as any).list
      total.value = (res as any).total
    }
    // Handle direct array { code: 200, data: [...] } or [...]
    else if (Array.isArray(res.data)) {
      appointmentList.value = res.data
      total.value = res.data.length
    } else if (Array.isArray(res)) {
      appointmentList.value = res
      total.value = res.length
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取预约列表失败')
  } finally {
    loading.value = false
  }
}

const fetchWaitingData = async () => {
  waitingLoading.value = true
  try {
    const res = await getMyWaitingList()
    // ResponseResult<WaitingListDTO[]>
    if (res && res.data) {
      waitingList.value = res.data
    } else {
      waitingList.value = []
    }
  } catch (error: any) {
    console.error('Fetch waiting list error:', error)
    const msg = error.message || error.msg || '获取候补列表失败'
    ElMessage.error(msg)
    // Ensure list is empty on error
    waitingList.value = []
  } finally {
    waitingLoading.value = false
  }
}

const handleConvert = async (item: WaitingListDTO) => {
  try {
    await ElMessageBox.confirm(t('waitingList.convertConfirm'), t('common.confirm'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })

    waitingLoading.value = true
    await convertWaitingList(item.id)
    ElMessage.success(t('common.success'))
    fetchWaitingData()
    fetchData() // Refresh appointments
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  } finally {
    waitingLoading.value = false
  }
}

const handleFilterChange = () => {
  pageNum.value = 1
  fetchData()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  pageNum.value = val
  fetchData()
}

const handlePay = async (item: AppointmentDTO) => {
  try {
    const res = await initiatePayment(item.id)
    const htmlForm =
      typeof res === 'string'
        ? res
        : typeof (res as any)?.data === 'string'
          ? (res as any).data
          : ''
    if (!htmlForm) {
      ElMessage.error('支付启动失败')
      return
    }
    const container = document.createElement('div')
    container.style.display = 'none'
    container.innerHTML = htmlForm
    document.body.appendChild(container)
    const form = container.querySelector('form')
    if (form) {
      ;(form as HTMLFormElement).submit()
    } else {
      ElMessage.error('支付表单无效')
    }
  } catch (e) {
    ElMessage.error('支付启动失败')
  }
}

const handleCancel = (item: AppointmentDTO) => {
  const diffHours = dayjs(item.startTime).diff(dayjs(), 'hour', true) // Float hours
  const isLate = diffHours < 12

  let confirmText = ''
  let type: 'warning' | 'info' | 'error' = 'info'

  if (item.paymentStatus === 'PAID') {
    let refundRate = 0
    // Fix: Align with backend 12h rule
    if (diffHours >= 2) refundRate = 1
    else refundRate = 0.5

    const refundAmount = (item.totalAmount * refundRate).toFixed(2)

    if (refundRate === 1) {
      if (diffHours >= 12) {
        confirmText = `您将获得全额退款 ¥${refundAmount}。确定要取消吗？`
        type = 'warning'
      } else {
        confirmText = `您将获得全额退款 ¥${refundAmount}。<br/><br/><span style="color: #F56C6C; font-weight: bold;">注意：距离服务时间不足12小时，本次取消将记录一次违约！</span><br/>累计3次违约将被限制预约。`
        type = 'error'
      }
    } else {
      confirmText = `距离服务时间不足2小时，将扣除50%费用，实际退款 ¥${refundAmount}。<br/><br/><span style="color: #F56C6C; font-weight: bold;">注意：本次取消将记录一次违约！</span><br/>累计3次违约将被限制预约。`
      type = 'error'
    }
  } else {
    // Unpaid
    if (isLate) {
      confirmText = `<span style="color: #F56C6C; font-weight: bold;">当前距离预约时间不足12小时，取消将记录一次违约！</span><br/><br/>累计3次违约将被限制预约。<br/>确定要继续吗？`
      type = 'warning'
    } else {
      confirmText = t('appointment.list.actions.cancelConfirm').replace(
        '(不足12小时将记录违约)',
        ''
      )
    }
  }

  ElMessageBox.confirm(confirmText, t('common.confirm'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: type,
    dangerouslyUseHTMLString: true
  })
    .then(async () => {
      try {
        await cancelAppointment(item.id)
        if (item.paymentStatus === 'PAID') {
          ElMessage.success(t('common.success'))
        } else {
          ElMessage.success(t('common.success'))
        }
        fetchData()
      } catch (error) {
        // Error handled by request interceptor usually
      }
    })
    .catch(() => {})
}

const handleReschedule = async (item: AppointmentDTO) => {
  rescheduleForm.id = item.id
  // Normalize to 'YYYY-MM-DD HH:mm:ss' to match backend @JsonFormat pattern
  rescheduleForm.newStartTime = dayjs(item.startTime).format('YYYY-MM-DD HH:mm:ss')
  // Try to find the tech from the first item if exists
  if (item.items && item.items.length > 0) {
    rescheduleForm.newTechId = item.items[0].techId
  } else {
    rescheduleForm.newTechId = undefined
  }

  // Load techs if not loaded
  if (techList.value.length === 0) {
    const res: any = await getTechnicianList()
    techList.value = res.data
  }

  rescheduleDialogVisible.value = true
}

const submitReschedule = async () => {
  if (!rescheduleForm.newStartTime) {
    ElMessage.warning(t('common.pleaseSelect'))
    return
  }
  try {
    // Ensure format is 'YYYY-MM-DD HH:mm:ss' regardless of picker output
    const formattedTime = dayjs(rescheduleForm.newStartTime).format('YYYY-MM-DD HH:mm:ss')
    await rescheduleAppointment(rescheduleForm.id, {
      newStartTime: formattedTime,
      newTechId: rescheduleForm.newTechId
    })
    ElMessage.success(t('common.success'))
    rescheduleDialogVisible.value = false
    fetchData()
  } catch (e) {
    // error handled
  }
}

onMounted(() => {
  fetchData()
  fetchWaitingData()
})
</script>

<style scoped>
.my-appointments-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.appointment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.appointment-card {
  transition: all 0.3s;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.card-body {
  padding: 10px 0;
}

.value-list {
  display: flex;
  flex-direction: column;
}

.service-item {
  margin-bottom: 4px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.info-row .label {
  width: 80px;
  color: #909399;
}

.price-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 10px;
  border-top: 1px solid #ebeef5;
  padding-top: 10px;
}

.price-row .price {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .filters {
    width: 100%;
    flex-direction: column;
  }

  .el-date-editor,
  .el-select {
    width: 100% !important;
    margin-right: 0 !important;
  }
}
</style>
