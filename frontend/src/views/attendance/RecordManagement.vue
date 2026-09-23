<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="queryParams.techName"
        :placeholder="$t('attendance.manager.name')"
        style="width: 200px"
        class="filter-item"
        @keyup.enter="handleFilter"
      />
      <el-select
        v-model="queryParams.status"
        :placeholder="$t('attendance.manager.status')"
        clearable
        class="filter-item"
        style="width: 130px; margin-left: 10px"
      >
        <el-option :label="$t('attendance.manager.statusType.normal')" value="NORMAL" />
        <el-option :label="$t('attendance.manager.statusType.late')" value="LATE" />
        <el-option :label="$t('attendance.manager.statusType.early')" value="EARLY_LEAVE" />
        <el-option :label="$t('attendance.manager.statusType.missing')" value="MISSING" />
      </el-select>
      <el-button class="filter-item" type="primary" style="margin-left: 10px" @click="handleFilter">
        {{ $t('common.search') }}
      </el-button>
      <el-button
        class="filter-item"
        type="success"
        style="margin-left: 10px"
        @click="handleClockIn('CLOCK_IN')"
      >
        {{ $t('attendance.clockIn') }}
      </el-button>
      <el-button
        class="filter-item"
        type="warning"
        style="margin-left: 10px"
        @click="handleClockIn('CLOCK_OUT')"
      >
        {{ $t('attendance.clockOut') }}
      </el-button>
    </div>

    <el-table
      v-loading="listLoading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%; margin-top: 20px"
    >
      <el-table-column
        :label="$t('attendance.manager.columns.id')"
        prop="id"
        align="center"
        width="80"
      />
      <el-table-column
        :label="$t('attendance.manager.columns.tech')"
        prop="techName"
        align="center"
      />
      <el-table-column :label="$t('attendance.manager.columns.type')" prop="type" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 'CLOCK_IN' ? 'success' : 'warning'">{{
            row.type === 'CLOCK_IN' ? $t('attendance.types.in') : $t('attendance.types.out')
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('attendance.manager.columns.time')"
        prop="time"
        align="center"
        width="160"
      />
      <el-table-column
        :label="$t('attendance.manager.columns.location')"
        prop="location"
        align="center"
      />
      <el-table-column
        :label="$t('attendance.manager.columns.status')"
        prop="status"
        align="center"
      >
        <template #default="{ row }">
          <el-tag :type="statusFilter(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('attendance.manager.columns.createTime')"
        prop="createTime"
        align="center"
        width="160"
      />
      <el-table-column :label="$t('common.operation')" align="center" width="200">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'NORMAL'"
            type="warning"
            size="small"
            @click="handleMarkException(row, 'LATE')"
          >
            {{ $t('attendance.manager.markLate') }}
          </el-button>
          <el-button
            v-if="row.status === 'LATE' || row.status === 'EARLY_LEAVE' || row.status === 'MISSING'"
            type="success"
            size="small"
            @click="handleMarkException(row, 'NORMAL')"
          >
            {{ $t('attendance.manager.markNormal') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  getAttendanceList,
  clockIn,
  updateAttendance,
  type AttendanceRecord,
  type AttendanceQueryDTO
} from '@/api/attendance'

const { t } = useI18n()
const list = ref<AttendanceRecord[]>([])
const listLoading = ref(true)
const queryParams = reactive<AttendanceQueryDTO>({
  techName: '',
  status: undefined,
  storeId: undefined
})

const statusFilter = (status: string) => {
  const statusMap: any = {
    NORMAL: 'success',
    LATE: 'danger',
    EARLY_LEAVE: 'warning',
    MISSING: 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusMap: any = {
    NORMAL: t('attendance.manager.statusType.normal'),
    LATE: t('attendance.manager.statusType.late'),
    EARLY_LEAVE: t('attendance.manager.statusType.early'),
    MISSING: t('attendance.manager.statusType.missing')
  }
  return statusMap[status] || t('common.unknown')
}

const getList = async () => {
  listLoading.value = true
  try {
    const response = await getAttendanceList(queryParams)
    list.value = (response as any).data || []
  } catch (error) {
    console.error(error)
  } finally {
    listLoading.value = false
  }
}

const handleFilter = () => {
  getList()
}

const handleClockIn = async (type: string) => {
  try {
    await clockIn({
      storeId: 1, // TODO: Get from user store
      type: type,
      location: '办公室' // Mock location
    })
    ElMessage.success(
      `${type === 'CLOCK_IN' ? t('attendance.clockIn') : t('attendance.clockOut')}${t('common.success')}`
    )
    getList()
  } catch (error) {
    console.error(error)
  }
}

const handleMarkException = async (row: AttendanceRecord, status: string) => {
  try {
    await updateAttendance({
      id: row.id,
      status: status
    })
    ElMessage.success(t('common.success'))
    getList()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.filter-container {
  padding-bottom: 10px;
}
.filter-item {
  display: inline-block;
  vertical-align: middle;
  margin-bottom: 10px;
}
</style>
