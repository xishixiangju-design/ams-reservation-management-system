<template>
  <div class="tech-selection">
    <!-- Tech Selection -->
    <div class="section">
      <h3>1. {{ $t('booking.step2').split('/')[0] }}</h3>
      <div class="tech-list">
        <!-- Any Tech Option -->
        <div class="tech-card" :class="{ active: bookingStore.isAnyTech }" @click="selectAnyTech">
          <div class="avatar-placeholder any-avatar">
            <el-icon><User /></el-icon>
          </div>
          <span class="name">{{ $t('booking.anyTech') }}</span>
          <span class="status">{{ $t('booking.recommend') }}</span>
        </div>

        <!-- Specific Techs -->
        <div
          v-for="tech in technicians"
          :key="tech.userId"
          class="tech-card"
          :class="{ active: bookingStore.selectedTech?.userId === tech.userId }"
          @click="selectTech(tech)"
        >
          <div class="avatar-placeholder">
            {{ tech.realName?.charAt(0) || '?' }}
          </div>
          <span class="name">{{ tech.realName || $t('common.unknown') }}</span>
          <span class="status">{{ getStatusText(tech.status) }}</span>
        </div>
      </div>
    </div>

    <!-- Date Selection -->
    <div class="section">
      <h3>2. {{ $t('booking.selectDate') }}</h3>
      <el-date-picker
        v-model="date"
        type="date"
        :placeholder="$t('booking.selectDate')"
        :disabled-date="disabledDate"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        style="width: 100%"
        @change="fetchSlots"
      />
    </div>

    <!-- Time Selection -->
    <div v-if="date" class="section">
      <h3>3. {{ $t('booking.selectTime') }}</h3>
      <div v-loading="slotsLoading" class="slots-grid">
        <el-empty
          v-if="!slotsLoading && slots.length === 0"
          :description="$t('appointment.create.time.noSlot')"
        />

        <div
          v-for="(slot, index) in slots"
          :key="index"
          class="time-slot"
          :class="{
            waitlist: !slot.available,
            active: bookingStore.selectedTime === slot.startTime
          }"
          @click="slot.available ? selectSlot(slot) : handleWaitlist(slot)"
        >
          <span class="time">{{ slot.startTime }}</span>
          <span v-if="slot.available && bookingStore.isAnyTech" class="sub-text">
            {{ $t('booking.remaining', { count: slot.availableTechCount }) }}
          </span>
          <span v-else-if="!slot.available" class="sub-text">{{ $t('booking.waitlist') }}</span>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <div class="footer-action">
      <el-button @click="prevStep">{{ $t('booking.prevStep') }}</el-button>
      <div v-if="canProceed" class="summary">{{ date }} {{ bookingStore.selectedTime }}</div>
      <el-button type="primary" :disabled="!canProceed" @click="nextStep">
        {{ $t('booking.nextStep') }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useBookingStore } from '@/stores/booking'
import { getTechnicianList, type TechnicianDTO } from '@/api/technician'
import { getAvailableSlots, type TimeSlotDTO, joinWaitlist } from '@/api/appointment'
import { User } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { ElMessageBox, ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const bookingStore = useBookingStore()
const { t } = useI18n()

const technicians = ref<TechnicianDTO[]>([])
const date = ref(bookingStore.selectedDate || '')
const slots = ref<TimeSlotDTO[]>([])
const slotsLoading = ref(false)

const canProceed = computed(() => {
  return (
    bookingStore.selectedDate &&
    bookingStore.selectedTime &&
    (bookingStore.selectedTech || bookingStore.isAnyTech)
  )
})

const fetchTechnicians = async () => {
  try {
    const res = await getTechnicianList()
    technicians.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch technicians:', error)
    technicians.value = []
  }
}

const selectAnyTech = () => {
  bookingStore.setTech(null, true)
  if (date.value) fetchSlots()
}

const selectTech = (tech: TechnicianDTO) => {
  bookingStore.setTech(tech, false)
  if (date.value) fetchSlots()
}

const disabledDate = (time: Date) => {
  // Disable past dates and dates more than 30 days ahead
  return dayjs(time).isBefore(dayjs(), 'day') || dayjs(time).isAfter(dayjs().add(30, 'day'), 'day')
}

const fetchSlots = async () => {
  if (!date.value) return

  // Clear previous selection if date changes
  if (date.value !== bookingStore.selectedDate) {
    bookingStore.setDateTime(date.value, '')
  }

  slotsLoading.value = true
  try {
    const res = await getAvailableSlots({
      date: date.value,
      serviceIds: bookingStore.selectedServices
        .map((s) => s.id)
        .filter((id): id is number => id !== undefined),
      techId: bookingStore.selectedTech?.userId,
      peopleCount: bookingStore.peopleCount
    })
    slots.value = res.data || []
  } finally {
    slotsLoading.value = false
  }
}

const selectSlot = (slot: TimeSlotDTO) => {
  if (!slot.available) return
  bookingStore.setDateTime(date.value, slot.startTime)
}

const handleWaitlist = (slot: TimeSlotDTO) => {
  ElMessageBox.confirm(t('booking.waitlistConfirm'), t('booking.joinWaitlist'), {
    confirmButtonText: t('booking.joinWaitlist'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
    .then(async () => {
      try {
        const duration = bookingStore.totalDuration || 60
        const startStr = slot.startTime
        const addMinutes = (time: string, mins: number) => {
          const [h, m] = time.split(':').map(Number)
          const date = new Date()
          date.setHours(h, m + mins)
          return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
        }
        const endStr = addMinutes(startStr, duration)
        const timeRange = `${startStr}-${endStr}`

        await joinWaitlist({
          serviceId: bookingStore.selectedServices[0].id!, // Note: Waitlist currently only tracks primary service
          techId: bookingStore.selectedTech?.userId,
          expectedDate: date.value,
          timeRange: timeRange,
          peopleCount: bookingStore.peopleCount
        })

        ElMessage.success(t('booking.waitlistSuccess'))
      } catch (e) {
        console.error(e)
      }
    })
    .catch(() => {
      // Cancelled
    })
}

const getStatusText = (status?: string) => {
  if (!status) return t('common.unknown')
  const map: Record<string, string> = {
    IDLE: t('technician.status.idle'),
    BUSY: t('technician.status.busy'),
    LEAVE: t('technician.status.leave')
  }
  return map[status] || status
}

const prevStep = () => {
  router.push({ name: 'booking-service' })
}

const nextStep = () => {
  router.push({ name: 'booking-confirm' })
}

onMounted(() => {
  if (bookingStore.selectedServices.length === 0) {
    router.replace({ name: 'booking-service' })
    return
  }
  fetchTechnicians()
  // If returning from next step
  if (bookingStore.selectedDate) {
    fetchSlots()
  }
})
</script>

<style scoped>
.section {
  margin-bottom: 30px;
}
.section h3 {
  margin-bottom: 15px;
  font-size: 16px;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

/* Tech List */
.tech-list {
  display: flex;
  gap: 15px;
  overflow-x: auto;
  padding-bottom: 10px;
  /* Hide scrollbar for cleaner look */
  scrollbar-width: none;
}
.tech-list::-webkit-scrollbar {
  display: none;
}

.tech-card {
  flex: 0 0 100px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.tech-card.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.avatar-placeholder {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #f2f6fc;
  margin: 0 auto 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: #909399;
}
.any-avatar {
  background-color: #e6a23c;
  color: #fff;
}

.name {
  display: block;
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 4px;
}

.status {
  font-size: 12px;
  color: #909399;
}

/* Slots Grid */
.slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 10px;
}

.time-slot {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px 4px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}

.time-slot:hover:not(.disabled) {
  border-color: #409eff;
  color: #409eff;
}

.time-slot.active {
  background-color: #409eff;
  color: #fff;
  border-color: #409eff;
}

.time-slot.waitlist {
  background-color: #fff6f6;
  color: #f56c6c;
  border-color: #fab6b6;
  cursor: pointer;
}
.time-slot.waitlist:hover {
  background-color: #fef0f0;
}

.time-slot.disabled {
  background-color: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
}

.time {
  display: block;
  font-weight: bold;
  font-size: 14px;
}

.sub-text {
  display: block;
  font-size: 10px;
  transform: scale(0.9);
}

.footer-action {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
