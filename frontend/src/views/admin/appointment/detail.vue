<template>
  <div class="appointment-detail-container">
    <el-page-header
      :title="$t('appointment.create.back')"
      :content="$t('appointment.detail.title')"
      class="mb-6"
      @back="$router.back()"
    />

    <el-row v-loading="loading" :gutter="20">
      <!-- Left: Main Info -->
      <el-col :span="16">
        <el-card class="mb-4">
          <template #header>
            <div class="card-header">
              <span class="font-bold">{{ $t('appointment.detail.baseInfo') }}</span>
              <el-tag :type="getStatusType(info.status)">{{ getStatusText(info.status) }}</el-tag>
            </div>
          </template>
          <el-descriptions border :column="2">
            <el-descriptions-item :label="$t('appointment.detail.orderNo')">{{
              info.id
            }}</el-descriptions-item>
            <el-descriptions-item :label="$t('appointment.detail.orderTime')">{{
              formatTime(info.createTime)
            }}</el-descriptions-item>
            <el-descriptions-item :label="$t('appointment.detail.time')"
              >{{ formatTime(info.startTime) }} -
              {{ formatTime(info.endTime) }}</el-descriptions-item
            >
            <el-descriptions-item
              :label="$t('appointment.detail.totalAmount')"
              label-class-name="text-price"
              >¥{{ info.totalAmount }}</el-descriptions-item
            >
            <el-descriptions-item :label="$t('appointment.detail.payStatus')">
              <el-tag :type="info.paymentStatus === 'PAID' ? 'success' : 'warning'">
                {{
                  info.paymentStatus === 'PAID'
                    ? $t('appointment.detail.paid')
                    : $t('appointment.detail.unpaid')
                }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('appointment.detail.payMethod')">
              <el-tag v-if="info.paymentMethod === 'OFFLINE'" type="info" effect="dark">{{
                $t('appointment.create.time.offline')
              }}</el-tag>
              <span v-else>{{ info.paymentMethod || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('appointment.detail.customerName')">{{
              info.customer?.nickname || $t('common.unknown')
            }}</el-descriptions-item>
            <el-descriptions-item :label="$t('appointment.detail.customerPhone')">{{
              info.customer?.username || $t('common.unknown')
            }}</el-descriptions-item>
            <el-descriptions-item :label="$t('appointment.detail.contact')">{{
              info.contactName || '-'
            }}</el-descriptions-item>
            <el-descriptions-item :label="$t('appointment.detail.contactPhone')">{{
              info.contactPhone || '-'
            }}</el-descriptions-item>
            <el-descriptions-item :label="$t('common.remark')" :span="2">{{
              info.remark || $t('common.noData')
            }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="mb-4">
          <template #header>
            <span class="font-bold">{{ $t('appointment.detail.serviceDetail') }}</span>
          </template>
          <el-table :data="info.items || []" border>
            <el-table-column :label="$t('appointment.detail.serviceName')">
              <template #default="scope">
                {{ scope.row.service?.name || scope.row.serviceId }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('appointment.detail.unitPrice')">
              <template #default="scope">¥{{ scope.row.price }}</template>
            </el-table-column>
            <el-table-column :label="$t('appointment.detail.tech')" prop="techId" />
            <el-table-column :label="$t('appointment.detail.room')" prop="roomId" />
          </el-table>
        </el-card>

        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane :label="$t('appointment.detail.logs')" name="logs">
              <el-empty :description="$t('common.noData')" />
            </el-tab-pane>
            <el-tab-pane :label="$t('appointment.detail.history')" name="history">
              <el-table :data="historyList" border size="small">
                <el-table-column prop="id" :label="$t('appointment.detail.orderNo')" width="80" />
                <el-table-column :label="$t('appointment.detail.time')" width="160">
                  <template #default="scope">{{ formatTime(scope.row.startTime) }}</template>
                </el-table-column>
                <el-table-column :label="$t('appointment.create.service.name')">
                  <!-- Simplified view -->
                  <template #default="scope"
                    >{{ scope.row.items?.length }}
                    {{
                      $t('appointment.create.service.selected', { count: '' }).replace(':', '')
                    }}</template
                  >
                </el-table-column>
                <el-table-column :label="$t('common.status')" width="100">
                  <template #default="scope">
                    <el-tag size="small" :type="getStatusType(scope.row.status)">{{
                      getStatusText(scope.row.status)
                    }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('common.operation')" width="80" fixed="right">
                  <template #default="scope">
                    <el-button
                      type="primary"
                      link
                      size="small"
                      @click="$router.push(`/admin/appointment/detail/${scope.row.id}`)"
                      >{{ $t('common.view') }}</el-button
                    >
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <!-- Right: Actions -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span class="font-bold">{{ $t('appointment.detail.actions') }}</span>
          </template>
          <div class="actions flex flex-col gap-4">
            <el-button
              v-if="info.paymentStatus !== 'PAID'"
              type="success"
              size="large"
              @click="handleMarkAsPaid"
              >{{ $t('appointment.detail.markPaid') }}</el-button
            >
            <el-button
              v-if="info.paymentStatus === 'PAID'"
              type="danger"
              size="large"
              @click="handleRevokePayment"
              >{{ $t('appointment.detail.revokePay') }}</el-button
            >
            <el-button
              type="primary"
              size="large"
              :disabled="info.status !== 5"
              @click="handleComplete"
              >{{ $t('appointment.detail.complete') }}</el-button
            >
            <el-button type="warning" size="large" disabled @click="handleReschedule">{{
              $t('appointment.detail.reschedule')
            }}</el-button>
            <el-button
              type="danger"
              size="large"
              :disabled="info.status !== 5"
              @click="handleCancel"
              >{{ $t('appointment.detail.cancel') }}</el-button
            >

            <div class="mt-4 text-gray text-sm">
              <p>{{ $t('appointment.detail.notes.title') }}</p>
              <p>{{ $t('appointment.detail.notes.1') }}</p>
              <p>{{ $t('appointment.detail.notes.2') }}</p>
              <p>{{ $t('appointment.detail.notes.3') }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  getAppointmentById,
  getCustomerAppointments,
  cancelAppointment,
  completeAppointmentByAdmin,
  markAsPaid,
  revokePayment,
  type AppointmentDTO
} from '@/api/appointment'
import dayjs from 'dayjs'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const info = ref<Partial<AppointmentDTO>>({})
const historyList = ref<AppointmentDTO[]>([])
const activeTab = ref('history')

const loadData = async () => {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const res = await getAppointmentById(id)
    info.value = res.data || {}

    // Load History if customer exists
    if (info.value.customerId) {
      const histRes = await getCustomerAppointments(info.value.customerId)
      historyList.value = histRes.data || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  if (!info.value.id) return
  ElMessageBox.confirm(t('appointment.list.actions.cancelConfirm'), t('common.confirm'), {
    confirmButtonText: t('appointment.create.submit'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  }).then(async () => {
    await cancelAppointment(info.value.id!)
    ElMessage.success(t('appointment.list.status.cancelled'))
    loadData()
  })
}

const handleComplete = async () => {
  if (!info.value.id) return
  try {
    await completeAppointmentByAdmin(info.value.id)
    ElMessage.success(t('appointment.list.status.completed'))
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleMarkAsPaid = async () => {
  if (!info.value.id) return
  try {
    await markAsPaid(info.value.id)
    ElMessage.success(t('appointment.detail.markPaid'))
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleRevokePayment = () => {
  if (!info.value.id) return
  ElMessageBox.prompt(t('common.pleaseInput'), t('appointment.detail.revokePay'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    inputPattern: /\S+/,
    inputErrorMessage: t('common.error')
  }).then(async ({ value }) => {
    try {
      await revokePayment(info.value.id!, value)
      ElMessage.success(t('common.success'))
      loadData()
    } catch (e) {
      console.error(e)
    }
  })
}

const handleReschedule = () => {
  ElMessage.info(t('common.loading'))
}

const formatTime = (time?: string) => (time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-')

const getStatusType = (status?: number): 'primary' | 'success' | 'info' | 'danger' | '' => {
  if (status === undefined) return 'info'
  const map: Record<number, string> = { 2: 'info', 3: 'info', 4: 'danger', 5: 'success' }
  return (map[status] || 'info') as 'primary' | 'success' | 'info' | 'danger' | ''
}

const getStatusText = (status?: number) => {
  if (status === undefined) return t('common.unknown')
  const map: Record<number, string> = {
    2: t('appointment.list.status.completed'),
    3: t('appointment.list.status.cancelled'),
    4: t('appointment.list.status.violation'),
    5: t('appointment.list.status.pending')
  }
  return map[status] || t('common.unknown')
}

onMounted(() => {
  loadData()
})

// Reload when route params change (e.g. clicking history item)
watch(
  () => route.params.id,
  () => {
    loadData()
  }
)
</script>

<style scoped>
.appointment-detail-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mb-6 {
  margin-bottom: 24px;
}
.mb-4 {
  margin-bottom: 16px;
}
.mt-4 {
  margin-top: 16px;
}
.text-price {
  color: #f56c6c;
  font-weight: bold;
}
.text-gray {
  color: #909399;
}
.gap-4 {
  gap: 1rem;
}
.flex-col {
  flex-direction: column;
}
.flex {
  display: flex;
}
</style>
