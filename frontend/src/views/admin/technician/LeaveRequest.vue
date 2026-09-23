<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="clearfix">
          <span>{{ $t('menu.myLeave') }}</span>
          <el-button
            class="leave-apply-button"
            type="success"
            size="large"
            style="float: right"
            @click="dialogVisible = true"
          >
            {{ $t('leave.button.apply') }}
          </el-button>
        </div>
      </template>

      <el-table :data="requestList" border stripe style="width: 100%">
        <el-table-column prop="id" :label="$t('leave.table.id')" width="80" />
        <el-table-column prop="type" :label="$t('leave.table.type')" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('leave.dialog.range')" min-width="200">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} {{ $t('common.to') }} {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" :label="$t('leave.table.reason')" show-overflow-tooltip />
        <el-table-column prop="status" :label="$t('leave.table.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('common.createTime')" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="$t('leave.dialog.create')" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item :label="$t('leave.table.type')">
          <el-select v-model="form.type" :placeholder="$t('common.pleaseSelect')">
            <el-option :label="$t('leave.type.sick')" value="SICK" />
            <el-option :label="$t('leave.type.casual')" value="CASUAL" />
            <el-option :label="$t('leave.type.annual')" value="ANNUAL" />
            <el-option :label="$t('leave.type.other')" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('leave.dialog.range')">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            :range-separator="$t('common.to')"
            :start-placeholder="$t('common.startTime')"
            :end-placeholder="$t('common.endTime')"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item :label="$t('leave.dialog.reason')">
          <el-input v-model="form.reason" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSubmit">{{ $t('common.submit') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { createLeaveRequest, getMyLeaveRequests, type LeaveRequest } from '@/api/technician'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'

const { t } = useI18n()
const requestList = ref<LeaveRequest[]>([])
const dialogVisible = ref(false)
const dateRange = ref<[string, string] | null>(null)

const form = reactive({
  type: '',
  reason: ''
})

const fetchRequests = () => {
  getMyLeaveRequests().then((res) => {
    requestList.value = res.data
  })
}

const handleSubmit = () => {
  if (!form.type || !dateRange.value) {
    ElMessage.warning(t('common.pleaseInput'))
    return
  }

  const data: LeaveRequest = {
    type: form.type,
    startTime: dateRange.value[0],
    endTime: dateRange.value[1],
    reason: form.reason
  }

  createLeaveRequest(data).then(() => {
    ElMessage.success(t('leave.dialog.success'))
    dialogVisible.value = false
    fetchRequests()
    // Reset form
    form.type = ''
    form.reason = ''
    dateRange.value = null
  })
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    APPROVED: 'success',
    REJECTED: 'danger',
    PENDING: 'warning'
  }
  return (map[status] || 'info') as any
}

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    SICK: t('leave.type.sick'),
    CASUAL: t('leave.type.casual'),
    ANNUAL: t('leave.type.annual'),
    OTHER: t('leave.type.other')
  }
  return map[type] || type
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    APPROVED: t('leave.filter.approved'),
    REJECTED: t('leave.filter.rejected'),
    PENDING: t('leave.filter.pending')
  }
  return map[status] || status
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchRequests()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>
