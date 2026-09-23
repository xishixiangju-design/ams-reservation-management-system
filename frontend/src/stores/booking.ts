import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import type { ServiceDTO } from '@/api/service'
import type { TechnicianDTO } from '@/api/technician'
import { ElMessage } from 'element-plus'
import { calculatePrice } from '@/api/pricing'

export const useBookingStore = defineStore('booking', () => {
  // State
  const selectedServices = ref<ServiceDTO[]>([])
  const selectedTech = ref<TechnicianDTO | null>(null) // null means "Any Technician" if explicitly chosen so, or just not selected yet?
  // Let's say: if user selects "Any", we might set a flag or keep this null but have a separate flag.
  // Requirement: "One-click switch to 'Any Technician' mode".
  // Let's add isAnyTech flag.
  const isAnyTech = ref(false)

  const peopleCount = ref(1)

  const selectedDate = ref<string>('')
  const selectedTime = ref<string>('')

  const customerInfo = ref({
    name: '',
    phone: '',
    remark: ''
  })

  // Computed
  const totalPrice = ref(0)

  watch(
    [selectedServices, peopleCount],
    async () => {
      if (selectedServices.value.length === 0) {
        totalPrice.value = 0
        return
      }

      let total = 0
      for (const service of selectedServices.value) {
        try {
          const res = await calculatePrice({
            serviceId: service.id!,
            peopleCount: peopleCount.value
          })
          if (res.data !== undefined) {
            total += res.data
          } else {
            // Fallback
            total += service.price * peopleCount.value
          }
        } catch (e) {
          console.error('Price calc failed', e)
          total += service.price * peopleCount.value
        }
      }
      // Round to 2 decimal places to avoid floating point errors
      totalPrice.value = Math.round(total * 100) / 100
    },
    { deep: true, immediate: true }
  )

  const totalDuration = computed(() => {
    return selectedServices.value.reduce((sum, s) => sum + s.duration, 0)
  })

  // Actions
  function toggleService(service: ServiceDTO) {
    const index = selectedServices.value.findIndex((s) => s.id === service.id)
    if (index > -1) {
      selectedServices.value.splice(index, 1)
    } else {
      if (selectedServices.value.length >= 5) {
        ElMessage.warning('最多只能选择5个套餐')
        return
      }

      // Mutex check
      if (service.mutexGroup) {
        const conflict = selectedServices.value.find((s) => s.mutexGroup === service.mutexGroup)
        if (conflict) {
          ElMessage.warning(`"${service.name}" 与已选的 "${conflict.name}" 互斥，无法同时选择`)
          return
        }
      }

      selectedServices.value.push(service)
    }
    // Reset subsequent steps
    selectedTech.value = null
    isAnyTech.value = false
    selectedDate.value = ''
    selectedTime.value = ''
  }

  function setTech(tech: TechnicianDTO | null, anyTech: boolean = false) {
    selectedTech.value = tech
    isAnyTech.value = anyTech
    // Reset time if tech changes
    selectedTime.value = ''
  }

  function setDateTime(date: string, time: string) {
    selectedDate.value = date
    selectedTime.value = time
  }

  function setCustomerInfo(info: { name: string; phone: string; remark: string }) {
    customerInfo.value = info
  }

  function reset() {
    selectedServices.value = []
    selectedTech.value = null
    isAnyTech.value = false
    selectedDate.value = ''
    selectedTime.value = ''
    peopleCount.value = 1
    customerInfo.value = { name: '', phone: '', remark: '' }
  }

  return {
    selectedServices,
    selectedTech,
    isAnyTech,
    selectedDate,
    selectedTime,
    customerInfo,
    totalPrice,
    totalDuration,
    toggleService,
    setTech,
    setDateTime,
    setCustomerInfo,
    reset,
    peopleCount,
    setPeopleCount
  }

  function setPeopleCount(count: number) {
    peopleCount.value = count
  }
})
