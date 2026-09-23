<template>
  <div class="service-selection">
    <div class="filter-bar">
      <el-input
        v-model="searchQuery"
        :placeholder="$t('common.search') + '...'"
        prefix-icon="Search"
        clearable
        class="search-input"
      />
      <div class="people-count">
        <span>{{ $t('booking.peopleCount') }}：</span>
        <el-input-number v-model="bookingStore.peopleCount" :min="1" :max="10" size="default" />
      </div>
    </div>

    <div v-loading="loading" class="service-list">
      <el-empty v-if="filteredServices.length === 0" :description="$t('common.noData')" />

      <div
        v-for="service in filteredServices"
        :key="service.id"
        class="service-card"
        :class="{ active: isSelected(service) }"
        @click="toggleService(service)"
      >
        <div class="service-info">
          <h3>{{ service.name }}</h3>
          <p class="desc">{{ service.description }}</p>
          <div class="meta">
            <span class="price">¥{{ service.price }}</span>
            <span class="duration">{{ service.duration }} min</span>
          </div>
        </div>
        <div class="action">
          <el-checkbox
            :model-value="isSelected(service)"
            @change="toggleService(service)"
            @click.stop
          >
            {{ isSelected(service) ? $t('common.selected') : $t('common.select') }}
          </el-checkbox>
        </div>
      </div>
    </div>

    <div class="footer-action">
      <div v-if="bookingStore.selectedServices.length > 0" class="summary">
        {{
          $t('appointment.create.service.selected', { count: bookingStore.selectedServices.length })
        }}
        <span class="total-price">¥{{ bookingStore.totalPrice.toFixed(2) }}</span>
      </div>
      <el-button
        type="primary"
        :disabled="bookingStore.selectedServices.length === 0"
        @click="nextStep"
      >
        {{ $t('booking.nextStep') }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getServiceList, type ServiceDTO } from '@/api/service'
import { useI18n } from 'vue-i18n'
import { useBookingStore } from '@/stores/booking'

const router = useRouter()
const bookingStore = useBookingStore()
const { t } = useI18n()
const loading = ref(false)
const services = ref<ServiceDTO[]>([])
const searchQuery = ref('')

const filteredServices = computed(() => {
  if (!searchQuery.value) return services.value
  return services.value.filter(
    (s) =>
      s.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      s.description?.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const fetchServices = async () => {
  loading.value = true
  try {
    const res = await getServiceList()
    if (res && Array.isArray((res as any).data)) {
      services.value = (res as any).data
    } else if (Array.isArray(res)) {
      services.value = res as any
    } else {
      services.value = []
    }
  } catch (error) {
    console.error(error)
    services.value = []
  } finally {
    loading.value = false
  }
}

const isSelected = (service: ServiceDTO) => {
  return bookingStore.selectedServices.some((s) => s.id === service.id)
}

const toggleService = (service: ServiceDTO) => {
  bookingStore.toggleService(service)
}

const nextStep = () => {
  if (bookingStore.selectedServices.length > 0) {
    router.push({ name: 'booking-tech' })
  }
}

onMounted(() => {
  fetchServices()
})
</script>

<style scoped>
.filter-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.people-count {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.service-list {
  display: grid;
  gap: 16px;
  /* Mobile first: 1 column */
  grid-template-columns: 1fr;
}

@media (min-width: 768px) {
  .service-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

.service-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.service-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.service-card.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.service-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
}

.desc {
  color: #909399;
  font-size: 13px;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  display: flex;
  gap: 12px;
  font-size: 14px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.duration {
  color: #606266;
}

.footer-action {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary {
  font-size: 14px;
  color: #606266;
}

.total-price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
  margin-left: 8px;
}
</style>
