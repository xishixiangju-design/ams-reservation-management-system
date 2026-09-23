<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="clearfix">
          <span>{{ $t('menu.myAttendance') }}</span>
        </div>
      </template>

      <div class="attendance-actions">
        <el-button type="primary" size="large" @click="handleClockIn">{{
          $t('attendance.clockIn')
        }}</el-button>
        <el-button type="success" size="large" @click="handleClockOut">{{
          $t('attendance.clockOut')
        }}</el-button>
      </div>

      <el-divider />

      <h3>{{ $t('attendance.todayRecord') }}</h3>
      <el-table :data="attendanceList" border stripe style="width: 100%">
        <el-table-column prop="id" :label="$t('attendance.columns.id')" width="80" />
        <el-table-column prop="type" :label="$t('attendance.columns.type')" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'CLOCK_IN' ? 'primary' : 'success'">
              {{ row.type === 'CLOCK_IN' ? $t('attendance.types.in') : $t('attendance.types.out') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" :label="$t('attendance.columns.time')">
          <template #default="{ row }">
            {{ formatTime(row.time) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { clockIn, clockOut, getMyAttendance, type Attendance } from '@/api/technician'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'

const { t } = useI18n()
const attendanceList = ref<Attendance[]>([])

const fetchAttendance = () => {
  const today = dayjs().format('YYYY-MM-DD')
  getMyAttendance(today).then((res) => {
    attendanceList.value = res.data
  })
}

const handleClockIn = () => {
  clockIn().then(() => {
    ElMessage.success(t('common.success'))
    fetchAttendance()
  })
}

const handleClockOut = () => {
  clockOut().then(() => {
    ElMessage.success(t('common.success'))
    fetchAttendance()
  })
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return dayjs(time).format('HH:mm:ss')
}

onMounted(() => {
  fetchAttendance()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.attendance-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  padding: 20px 0;
}
</style>
