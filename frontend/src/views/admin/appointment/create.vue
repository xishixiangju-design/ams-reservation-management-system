<template>
  <div class="create-appointment-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('appointment.create.title') }}</span>
          <el-button @click="$router.back()">{{ $t('appointment.create.back') }}</el-button>
        </div>
      </template>

      <el-steps :active="activeStep" finish-status="success" align-center class="mb-20">
        <el-step
          :title="$t('appointment.create.steps.customer')"
          :description="$t('appointment.create.steps.customer')"
        />
        <el-step
          :title="$t('appointment.create.steps.service')"
          :description="$t('appointment.create.steps.service')"
        />
        <el-step
          :title="$t('appointment.create.steps.time')"
          :description="$t('appointment.create.steps.time')"
        />
        <el-step
          :title="$t('appointment.create.steps.confirm')"
          :description="$t('appointment.create.steps.confirm')"
        />
      </el-steps>

      <div class="step-content">
        <!-- Step 1: Select Customer -->
        <div v-if="activeStep === 0" class="step-pane">
          <el-form :model="form" label-width="100px">
            <el-form-item :label="$t('appointment.create.customer.label')" required>
              <el-select
                v-model="form.customerId"
                filterable
                remote
                :placeholder="$t('appointment.create.customer.placeholder')"
                :remote-method="handleCustomerSearch"
                :loading="customerLoading"
                style="width: 100%"
                @change="handleCustomerSelect"
              >
                <el-option
                  v-for="item in customerOptions"
                  :key="item.id"
                  :label="`${item.nickname} (${item.username})`"
                  :value="item.id"
                />
              </el-select>
              <div class="mt-2 text-gray">
                {{ $t('appointment.create.customer.notFound') }}
              </div>
            </el-form-item>

            <div v-if="selectedCustomer" class="customer-preview mt-4">
              <el-descriptions :title="$t('appointment.create.customer.info')" border>
                <el-descriptions-item :label="$t('appointment.create.customer.nickname')">{{
                  selectedCustomer.nickname
                }}</el-descriptions-item>
                <el-descriptions-item :label="$t('appointment.create.customer.phone')">{{
                  selectedCustomer.username
                }}</el-descriptions-item>
                <el-descriptions-item :label="$t('appointment.create.customer.regTime')">{{
                  formatTime(selectedCustomer.createTime)
                }}</el-descriptions-item>
              </el-descriptions>
            </div>

            <el-form-item
              v-if="!form.customerId"
              :label="$t('appointment.create.customer.contactName')"
            >
              <el-input
                v-model="form.contactName"
                :placeholder="$t('appointment.create.customer.guestName')"
              />
            </el-form-item>
            <el-form-item
              v-if="!form.customerId"
              :label="$t('appointment.create.customer.contactPhone')"
            >
              <el-input
                v-model="form.contactPhone"
                :placeholder="$t('appointment.create.customer.guestPhone')"
              />
            </el-form-item>
          </el-form>
        </div>

        <!-- Step 2: Select Service (Multi-select) -->
        <div v-if="activeStep === 1" class="step-pane">
          <el-alert
            :title="$t('appointment.create.service.alert')"
            type="info"
            show-icon
            class="mb-20"
          />
          <el-table
            ref="serviceTableRef"
            :data="serviceList"
            border
            style="width: 100%"
            row-key="id"
            @selection-change="handleServiceSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="name" :label="$t('appointment.create.service.name')" />
            <el-table-column
              prop="duration"
              :label="$t('appointment.create.service.duration')"
              width="120"
              sortable
            />
            <el-table-column
              prop="price"
              :label="$t('appointment.create.service.price')"
              width="120"
              sortable
            />
            <el-table-column prop="description" :label="$t('appointment.create.service.desc')" />
          </el-table>
          <div class="mt-4 text-right">
            {{ $t('appointment.create.service.selected', { count: selectedServices.length }) }}
          </div>
        </div>

        <!-- Step 3: Select Time (Multi-select) -->
        <div v-if="activeStep === 2" class="step-pane">
          <el-form :model="form" label-width="100px">
            <el-form-item :label="$t('appointment.create.time.date')" required>
              <el-date-picker
                v-model="selectedDate"
                type="date"
                :placeholder="$t('common.pleaseSelect')"
                value-format="YYYY-MM-DD"
                :disabled-date="disabledDate"
                @change="fetchSlots"
              />
            </el-form-item>
            <el-form-item :label="$t('appointment.create.time.tech')">
              <el-select
                v-model="form.techId"
                :placeholder="$t('appointment.create.time.techPlaceholder')"
                clearable
                @change="fetchSlots"
              >
                <el-option
                  v-for="tech in techList"
                  :key="tech.userId"
                  :label="tech.sysUser?.nickname || tech.realName"
                  :value="tech.userId"
                />
              </el-select>
              <div class="text-gray text-xs mt-1">{{ $t('appointment.create.time.techNote') }}</div>
            </el-form-item>

            <el-form-item :label="$t('appointment.create.time.slot')" required>
              <div v-if="!selectedDate" class="text-gray">
                {{ $t('appointment.create.time.noDate') }}
              </div>
              <div v-else-if="slotLoading" class="text-gray">{{ $t('common.loading') }}</div>
              <div v-else-if="slots.length === 0" class="text-error">
                {{ $t('appointment.create.time.noSlot') }}
              </div>
              <div v-else class="slots-container">
                <el-check-tag
                  v-for="slot in slots"
                  :key="slot.startTime"
                  :checked="selectedTimes.includes(slot.startTime)"
                  :disabled="!slot.available"
                  class="m-1"
                  @change="toggleTime(slot.startTime)"
                >
                  {{ slot.startTime.substring(0, 5) }}
                </el-check-tag>
              </div>
              <div class="mt-2 text-info">
                <el-tag type="info">{{
                  $t('appointment.create.time.selected', { count: selectedTimes.length })
                }}</el-tag>
                <span v-if="selectedServices.length > 1" class="ml-2 text-warning">
                  {{
                    $t('appointment.create.time.summary', {
                      serviceCount: selectedServices.length,
                      timeCount: selectedTimes.length,
                      total: selectedServices.length * selectedTimes.length
                    })
                  }}
                </span>
              </div>
            </el-form-item>

            <el-form-item :label="$t('common.remark')">
              <el-input v-model="form.remark" type="textarea" />
            </el-form-item>

            <el-form-item :label="$t('appointment.create.time.payment')">
              <el-tag type="info" effect="dark">{{ $t('appointment.create.time.offline') }}</el-tag>
              <span class="text-gray text-xs ml-2">{{
                $t('appointment.create.time.offlineNote')
              }}</span>
            </el-form-item>
          </el-form>
        </div>

        <!-- Step 4: Confirmation -->
        <div v-if="activeStep === 3" class="step-pane">
          <el-descriptions :title="$t('appointment.create.confirm.title')" border :column="1">
            <el-descriptions-item :label="$t('appointment.create.confirm.customer')"
              >{{ selectedCustomer?.nickname }} ({{
                selectedCustomer?.username
              }})</el-descriptions-item
            >
            <el-descriptions-item :label="$t('appointment.create.confirm.service')">
              <el-tag v-for="s in selectedServices" :key="s.id" class="mr-2">{{ s.name }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('appointment.create.confirm.time')">
              <div v-for="t in selectedTimes" :key="t">{{ selectedDate }} {{ t }}</div>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('appointment.create.confirm.tech')">{{
              getTechName(form.techId)
            }}</el-descriptions-item>
            <el-descriptions-item :label="$t('appointment.create.confirm.total')">
              <span class="text-xl">{{ selectedServices.length * selectedTimes.length }}</span>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('common.remark')">{{
              form.remark || $t('common.noData')
            }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <div class="action-footer mt-6 flex justify-end">
        <el-button v-if="activeStep > 0" @click="prevStep">{{
          $t('appointment.create.prevStep')
        }}</el-button>
        <el-button v-if="activeStep < 3" type="primary" :disabled="!canProceed" @click="nextStep">{{
          $t('appointment.create.nextStep')
        }}</el-button>
        <el-button
          v-if="activeStep === 3"
          type="success"
          :loading="submitLoading"
          @click="submitBooking"
          >{{ $t('appointment.create.submit') }}</el-button
        >
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElTable } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { searchCustomers, type CustomerDTO } from '@/api/customer'
import { getServiceList, type ServiceDTO } from '@/api/service'
import { getTechnicianList, type TechnicianDTO } from '@/api/technician'
import {
  getAvailableSlots,
  batchCreateAppointment,
  batchPayAppointment,
  type TimeSlotDTO,
  type BatchBookingRequest
} from '@/api/appointment'
import dayjs from 'dayjs'

const { t } = useI18n()
const router = useRouter()
const activeStep = ref(0)
const submitLoading = ref(false)

// Form Data
const form = reactive({
  customerId: undefined as number | undefined,
  contactName: '',
  contactPhone: '',
  serviceIds: [] as number[],
  techId: undefined as number | undefined,
  remark: ''
})

const selectedDate = ref('')
const selectedTimes = ref<string[]>([])

// Data Sources
const customerOptions = ref<CustomerDTO[]>([])
const customerLoading = ref(false)
const serviceList = ref<ServiceDTO[]>([])
const techList = ref<TechnicianDTO[]>([])
const slots = ref<TimeSlotDTO[]>([])
const slotLoading = ref(false)

// Selections
const selectedCustomer = computed(() => customerOptions.value.find((c) => c.id === form.customerId))
const selectedServices = computed(() =>
  serviceList.value.filter((s) => form.serviceIds.includes(s.id))
)

// Step Validation
const canProceed = computed(() => {
  if (activeStep.value === 0) {
    return !!form.customerId || (!!form.contactName && !!form.contactPhone)
  }
  if (activeStep.value === 1) return form.serviceIds.length > 0
  if (activeStep.value === 2) return !!selectedDate.value && selectedTimes.value.length > 0
  return true
})

// Methods
const handleCustomerSearch = async (query: string) => {
  if (query) {
    customerLoading.value = true
    try {
      const res = await searchCustomers(query)
      customerOptions.value = res.data
    } finally {
      customerLoading.value = false
    }
  } else {
    customerOptions.value = []
  }
}

const handleCustomerSelect = (val: number) => {
  // Logic if needed
}

const handleServiceSelectionChange = (rows: ServiceDTO[]) => {
  form.serviceIds = rows.map((r) => r.id)
}

const fetchServices = async () => {
  const res = await getServiceList()
  serviceList.value = res.data || []
}

const fetchTechs = async () => {
  const res = await getTechnicianList()
  techList.value = res.data || []
}

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7 // Disable past dates
}

const fetchSlots = async () => {
  if (!selectedDate.value || form.serviceIds.length === 0) return

  slotLoading.value = true
  selectedTimes.value = [] // Reset time selection
  try {
    // Logic: Pick service with MAX duration to ensure availability
    const maxDurationService = selectedServices.value.reduce(
      (prev, current) => (prev.duration > current.duration ? prev : current),
      selectedServices.value[0]
    )

    const res = await getAvailableSlots({
      date: selectedDate.value,
      serviceId: maxDurationService.id, // Use max duration service
      techId: form.techId
    })
    slots.value = res.data || []
  } finally {
    slotLoading.value = false
  }
}

const toggleTime = (time: string) => {
  const index = selectedTimes.value.indexOf(time)
  if (index > -1) {
    selectedTimes.value.splice(index, 1)
  } else {
    selectedTimes.value.push(time)
  }
}

const getTechName = (techId?: number) => {
  if (!techId) return t('appointment.create.time.techPlaceholder')
  const tech = techList.value.find((t) => t.userId === techId)
  return tech ? tech.sysUser?.nickname || tech.realName : t('common.unknown')
}

const formatTime = (timeStr: string) => {
  return dayjs(timeStr).format('YYYY-MM-DD HH:mm')
}

const prevStep = () => {
  if (activeStep.value > 0) activeStep.value--
}

const nextStep = () => {
  if (activeStep.value < 3) activeStep.value++
}

const submitBooking = async () => {
  const hasCustomer = !!form.customerId
  const hasContact = !!(form.contactName && form.contactPhone)

  if (!hasCustomer && !hasContact) {
    ElMessage.warning(t('appointment.create.customer.notFound'))
    return
  }

  submitLoading.value = true
  try {
    // Prepare full datetime strings
    const startTimes = selectedTimes.value.map((t) => `${selectedDate.value}T${t}`)

    const req: BatchBookingRequest = {
      customerId: form.customerId,
      startTimes: startTimes,
      serviceIds: form.serviceIds,
      remark: form.remark,
      contactName: form.contactName,
      contactPhone: form.contactPhone,
      techId: form.techId
    }

    const res = await batchCreateAppointment(req)
    const createdIds = res.data || []

    // Prompt for Payment
    ElMessageBox.confirm(
      t('appointment.create.success', { count: createdIds.length }),
      t('common.confirm'),
      {
        confirmButtonText: t('appointment.create.payNow'),
        cancelButtonText: t('appointment.create.payLater'),
        type: 'success'
      }
    )
      .then(async () => {
        // Pay
        await batchPayAppointment(createdIds)
        ElMessage.success(t('common.success'))
        router.push('/admin/appointment')
      })
      .catch(() => {
        ElMessage.info(t('common.cancel'))
        router.push('/admin/appointment')
      })
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || t('common.error'))
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchServices()
  fetchTechs()
})
</script>

<style scoped>
.create-appointment-container {
  padding: 20px;
}
.step-content {
  margin-top: 30px;
  min-height: 300px;
}
.mb-20 {
  margin-bottom: 20px;
}
.mt-4 {
  margin-top: 16px;
}
.mt-6 {
  margin-top: 24px;
}
.m-1 {
  margin: 4px;
}
.text-gray {
  color: #909399;
}
.text-error {
  color: #f56c6c;
}
.text-price {
  color: #f56c6c;
  font-weight: bold;
}
.slots-container {
  display: flex;
  flex-wrap: wrap;
}
.text-info {
  color: #909399;
  font-size: 0.9em;
}
.text-warning {
  color: #e6a23c;
}
</style>
