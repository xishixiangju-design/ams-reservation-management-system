<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getSummary, getRoomStatus, getTechStatus, getCalendarStats } from '@/api/dashboard'
import type { DashboardSummary, RoomStatus, TechStatus } from '@/api/dashboard'
import dayjs from 'dayjs'

const summary = ref<DashboardSummary>({
  todayAppointments: 0,
  todayRevenue: 0,
  waitlistCount: 0,
  violationCount: 0,
  pendingServicesCount: 0
})

const roomList = ref<RoomStatus[]>([])
const techList = ref<TechStatus[]>([])
const timer = ref<number | null>(null)

// Calendar Logic
const calendarDate = ref(new Date())
const dailyStats = ref<Record<string, { count: number }>>({})

const fetchMonthData = async (date: Date) => {
  const start = dayjs(date).startOf('month').format('YYYY-MM-DD HH:mm:ss')
  const end = dayjs(date).endOf('month').format('YYYY-MM-DD HH:mm:ss')

  try {
    const res = await getCalendarStats(start, end)
    const stats: Record<string, { count: number }> = {}
    if (res.data) {
      res.data.forEach((item: any) => {
        stats[item.date] = { count: item.count }
      })
    }
    dailyStats.value = stats
  } catch (e) {
    console.error('Failed to fetch calendar data', e)
  }
}

const fetchData = async () => {
  try {
    const [summaryRes, roomRes, techRes] = await Promise.all([
      getSummary(),
      getRoomStatus(),
      getTechStatus()
    ])
    summary.value = summaryRes.data
    roomList.value = roomRes.data
    techList.value = techRes.data
  } catch (error) {
    console.error('Failed to fetch dashboard data', error)
  }
}

// Watch for calendar changes (month navigation)
import { watch } from 'vue'
watch(calendarDate, (newDate) => {
  fetchMonthData(newDate)
})

onMounted(() => {
  fetchData()
  fetchMonthData(calendarDate.value)
  timer.value = window.setInterval(fetchData, 30000)
})

onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
})

const getStatusColor = (status: string) => {
  switch (status) {
    case 'IDLE':
      return 'success'
    case 'OCCUPIED':
      return 'danger'
    case 'BUSY':
      return 'danger'
    case 'CLEANING':
      return 'warning'
    case 'LEAVE':
      return 'info'
    default:
      return 'info'
  }
}
</script>

<template>
  <div class="dashboard-container">
    <!-- Summary Cards -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>{{ $t('dashboard.todayAppt') }}</template>
          <div class="card-value">{{ summary.todayAppointments }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>{{ $t('dashboard.todayRevenue') }}</template>
          <div class="card-value">¥{{ summary.todayRevenue }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>{{ $t('dashboard.waitlist') }}</template>
          <div class="card-value">{{ summary.waitlistCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>{{ $t('dashboard.pending') }}</template>
          <div class="card-value">{{ summary.pendingServicesCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- Room Status -->
      <el-col :span="16">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>{{ $t('dashboard.roomStatus') }}</span>
              <el-tag type="info" size="small">{{ $t('dashboard.refresh') }}</el-tag>
            </div>
          </template>
          <div class="room-grid">
            <el-card
              v-for="room in roomList"
              :key="room.id"
              class="room-card"
              :class="['status-' + room.status.toLowerCase()]"
              shadow="hover"
            >
              <div class="room-header">
                <span class="room-name">{{ room.name }}</span>
                <el-tag size="small" type="info">{{ room.type }}</el-tag>
              </div>
              <div class="room-status">
                <div v-if="room.status === 'OCCUPIED'">
                  <div class="customer">{{ room.currentCustomer }}</div>
                  <div class="time">{{ room.remainingTime }}</div>
                </div>
                <div v-else class="idle-text">
                  {{
                    room.status === 'CLEANING'
                      ? $t('dashboard.room.cleaning')
                      : $t('dashboard.room.idle')
                  }}
                </div>
              </div>
            </el-card>
          </div>
        </el-card>
      </el-col>

      <!-- Tech Status -->
      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <span>{{ $t('dashboard.techStatus') }}</span>
          </template>
          <el-table :data="techList" style="width: 100%" size="small">
            <el-table-column prop="wheelSeq" :label="$t('dashboard.tech.seq')" width="50" />
            <el-table-column prop="name" :label="$t('dashboard.tech.name')" width="100" />
            <el-table-column :label="$t('dashboard.tech.status')">
              <template #default="{ row }">
                <el-tag :type="getStatusColor(row.status)" size="small">
                  {{
                    row.status === 'BUSY'
                      ? $t('appointment.list.status.busy')
                      : row.status === 'LEAVE'
                        ? $t('dashboard.tech.leave')
                        : $t('dashboard.tech.idle')
                  }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="$t('dashboard.tech.detail')" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.currentTask || '-' }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- Calendar View -->
    <el-row class="mt-4">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>{{ $t('dashboard.calendar') }}</span>
          </template>
          <el-calendar v-model="calendarDate">
            <template #date-cell="{ data }">
              <div class="calendar-cell">
                <span>{{ data.day.split('-').slice(2).join('') }}</span>
                <div v-if="dailyStats[data.day]" class="daily-stat">
                  <el-tag size="small" type="primary" effect="light">
                    {{ dailyStats[data.day].count }} {{ $t('dashboard.apptUnit') }}
                  </el-tag>
                </div>
              </div>
            </template>
          </el-calendar>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: 20px;
}
.mb-4 {
  margin-bottom: 20px;
}
.mt-4 {
  margin-top: 20px;
}
.calendar-cell {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.daily-stat {
  margin-top: 5px;
}
.card-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #409eff;
}
.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
}
.room-card {
  text-align: center;
  transition: all 0.3s;
}
.room-card.status-occupied {
  border-color: #f56c6c;
  background-color: #fef0f0;
}
.room-card.status-idle {
  border-color: #67c23a;
  background-color: #f0f9eb;
}
.room-card.status-cleaning {
  border-color: #e6a23c;
  background-color: #fdf6ec;
}
.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.room-name {
  font-weight: bold;
  font-size: 16px;
}
.customer {
  font-weight: bold;
  color: #303133;
}
.time {
  font-size: 12px;
  color: #f56c6c;
}
.idle-text {
  color: #909399;
  padding: 10px 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
