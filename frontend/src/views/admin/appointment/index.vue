<template>
  <div class="app-container">
    <el-card shadow="never">
      <!-- 预约列表 -->
      <el-form :inline="true" :model="appointmentQuery" class="demo-form-inline">
        <el-form-item :label="$t('common.keyword')">
          <el-input
            v-model="appointmentQuery.keyword"
            :placeholder="$t('appointment.list.keywordPlaceholder')"
            clearable
            @keyup.enter="handleAppointmentQuery"
          />
        </el-form-item>
        <el-form-item :label="$t('common.timeRange')">
          <el-date-picker
            v-model="appointmentDateRange"
            type="datetimerange"
            :range-separator="$t('common.to')"
            :start-placeholder="$t('common.startTime')"
            :end-placeholder="$t('common.endTime')"
            value-format="YYYY-MM-DD HH:mm:ss"
            :default-time="defaultTime"
          />
        </el-form-item>
        <el-form-item :label="$t('common.status')">
          <el-select
            v-model="appointmentQuery.status"
            :placeholder="$t('appointment.list.status.all')"
            clearable
            style="width: 120px"
          >
            <!-- 0 and 1 removed -->
            <el-option :label="$t('appointment.list.status.completed')" :value="2" />
            <el-option :label="$t('appointment.list.status.cancelled')" :value="3" />
            <el-option :label="$t('appointment.list.status.violation')" :value="4" />
            <el-option :label="$t('appointment.list.status.pending')" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAppointmentQuery">{{
            $t('common.search')
          }}</el-button>
          <el-button @click="resetAppointmentQuery">{{ $t('common.reset') }}</el-button>
          <el-button type="success" @click="handleAdd">{{
            $t('appointment.create.title')
          }}</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="appointmentList" border stripe>
        <el-table-column
          :label="$t('appointment.list.columns.id')"
          prop="id"
          width="80"
          align="center"
        />
        <el-table-column :label="$t('appointment.list.columns.customer')" min-width="160">
          <template #default="{ row }">
            <div>
              <span v-if="row.contactName">{{ row.contactName }}</span>
              <span v-else>{{ row.customer?.nickname || row.customer?.username }}</span>
              <br />
              <span class="text-gray-500 text-xs">{{
                row.contactPhone || row.customer?.username
              }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('appointment.list.columns.time')" min-width="180">
          <template #default="{ row }">
            <div>{{ formatTime(row.startTime) }}</div>
            <div class="text-gray-400 text-xs">
              {{ $t('common.to') }} {{ formatTime(row.endTime) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('appointment.list.columns.service')" min-width="120">
          <template #default="{ row }">
            <div>{{ row.peopleCount }} {{ $t('common.people') || '人' }}</div>
            <div class="text-xs text-gray-500">￥{{ row.totalAmount }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('appointment.list.columns.status')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('appointment.list.columns.remark')"
          prop="remark"
          show-overflow-tooltip
        />
        <el-table-column
          :label="$t('appointment.list.columns.createTime')"
          prop="createTime"
          width="160"
        />
        <el-table-column :label="$t('common.operation')" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">{{
              $t('appointment.list.actions.detail')
            }}</el-button>
            <el-popconfirm
              v-if="row.status === 5"
              :title="$t('appointment.list.actions.cancelConfirm')"
              @confirm="handleCancel(row)"
            >
              <template #reference>
                <el-button link type="danger">{{
                  $t('appointment.list.actions.cancel')
                }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="appointmentQuery.pageNum"
          v-model:page-size="appointmentQuery.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="appointmentTotal"
          @size-change="handleAppointmentQuery"
          @current-change="handleAppointmentQuery"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, type Ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  getAdminAppointmentList,
  cancelAppointment,
  type AppointmentQuery,
  type AppointmentDTO
} from '@/api/appointment'
import { useRequest } from '@/hooks/useRequest' // Import the hook
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const { t } = useI18n()
const router = useRouter()

// --- Appointment Logic ---
const appointmentList: Ref<AppointmentDTO[]> = ref([])
const appointmentTotal = ref(0)
const appointmentDateRange = ref<[Date, Date] | null>(null)
const defaultTime: [Date, Date] = [new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]

const appointmentQuery = reactive<AppointmentQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: undefined,
  startDate: undefined,
  endDate: undefined
})

const getStatusLabel = (status: number) => {
  const map: Record<number, string> = {
    2: t('appointment.list.status.completed'),
    3: t('appointment.list.status.cancelled'),
    4: t('appointment.list.status.violation'),
    5: t('appointment.list.status.pending')
  }
  return map[status] || t('common.unknown')
}

const getStatusType = (status: number): 'success' | 'info' | 'danger' | 'warning' | 'primary' => {
  const map: Record<number, 'success' | 'info' | 'danger' | 'success'> = {
    2: 'success',
    3: 'info',
    4: 'danger',
    5: 'success'
  }
  return map[status] || 'info'
}

// Use useRequest hook
const { loading, run: fetchAppointments } = useRequest(getAdminAppointmentList, {
  debounce: 300,
  onSuccess: (res) => {
    // res is ResponseResult, but useRequest unwraps .data?
    // Wait, let's check useRequest implementation.
    // It does data.value = res.data.
    // So if res (from API) is ResponseResult, then res.data is { list, total }.
    // BUT onSuccess receives 'res' which is the return value of apiFn?
    // In my implementation:
    // const res = await apiFn(...)
    // data.value = res.data
    // options.onSuccess?.(res) -> passes the full response
    // So here 'res' is ResponseResult.
    if (res && res.data) {
      appointmentList.value = res.data.list
      appointmentTotal.value = res.data.total
    }
  },
  onError: (err) => {
    console.error('Failed to fetch appointments:', err)
  }
})

const handleAppointmentQuery = () => {
  if (appointmentDateRange.value && appointmentDateRange.value.length === 2) {
    appointmentQuery.startDate = dayjs(appointmentDateRange.value[0]).format('YYYY-MM-DD HH:mm:ss')
    appointmentQuery.endDate = dayjs(appointmentDateRange.value[1]).format('YYYY-MM-DD HH:mm:ss')
  } else {
    appointmentQuery.startDate = undefined
    appointmentQuery.endDate = undefined
  }

  fetchAppointments(appointmentQuery)
}

const resetAppointmentQuery = () => {
  appointmentQuery.keyword = ''
  appointmentQuery.status = undefined
  appointmentDateRange.value = null
  appointmentQuery.pageNum = 1
  handleAppointmentQuery()
}

const handleAdd = () => {
  router.push('/admin/appointment/create')
}

const handleDetail = (row: any) => {
  router.push(`/admin/appointment/detail/${row.id}`)
}

const handleCancel = (row: any) => {
  cancelAppointment(row.id).then(() => {
    ElMessage.success(t('common.success'))
    handleAppointmentQuery()
  })
}

// Common Utils
const formatTime = (time: string) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  handleAppointmentQuery()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
