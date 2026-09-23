<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="queryParams.techName"
        :placeholder="$t('leave.filter.techName')"
        style="width: 200px"
        class="filter-item"
        @keyup.enter="handleFilter"
      />
      <el-select
        v-model="queryParams.status"
        :placeholder="$t('leave.filter.status')"
        clearable
        class="filter-item"
        style="width: 130px; margin-left: 10px"
      >
        <el-option :label="$t('leave.filter.pending')" value="PENDING" />
        <el-option :label="$t('leave.filter.approved')" value="APPROVED" />
        <el-option :label="$t('leave.filter.rejected')" value="REJECTED" />
      </el-select>
      <el-button class="filter-item" type="primary" style="margin-left: 10px" @click="handleFilter">
        {{ $t('common.search') }}
      </el-button>
      <el-button class="filter-item" type="success" style="margin-left: 10px" @click="handleCreate">
        {{ $t('leave.button.apply') }}
      </el-button>
      <el-button class="filter-item" type="warning" style="margin-left: 10px" @click="handleExport">
        {{ $t('leave.button.export') }}
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
      <el-table-column :label="$t('leave.table.id')" prop="id" align="center" width="80" />
      <el-table-column :label="$t('leave.table.techName')" prop="techName" align="center" />
      <el-table-column :label="$t('leave.table.type')" prop="type" align="center">
        <template #default="{ row }">
          <el-tag>{{ getTypeText(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('leave.table.startTime')"
        prop="startTime"
        align="center"
        width="160"
      />
      <el-table-column
        :label="$t('leave.table.endTime')"
        prop="endTime"
        align="center"
        width="160"
      />
      <el-table-column :label="$t('leave.table.reason')" prop="reason" align="center" />
      <el-table-column :label="$t('leave.table.status')" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="statusFilter(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('leave.table.createTime')"
        prop="createTime"
        align="center"
        width="160"
      />
      <el-table-column
        :label="$t('common.operation')"
        align="center"
        width="230"
        class-name="small-padding fixed-width"
      >
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button type="success" size="small" @click="handleApprove(row)">
              {{ $t('leave.button.approve') }}
            </el-button>
            <el-button type="danger" size="small" @click="handleReject(row)">
              {{ $t('leave.button.reject') }}
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!-- Apply Leave Dialog -->
    <el-dialog v-model="dialogFormVisible" :title="textMap[dialogStatus]">
      <el-form
        ref="dataFormRef"
        :rules="rules"
        :model="temp"
        label-position="left"
        label-width="100px"
        style="width: 400px; margin-left: 50px"
      >
        <el-form-item :label="$t('leave.table.type')" prop="type">
          <el-select
            v-model="temp.type"
            class="filter-item"
            :placeholder="$t('common.pleaseSelect')"
          >
            <el-option :label="$t('leave.type.sick')" value="SICK" />
            <el-option :label="$t('leave.type.casual')" value="CASUAL" />
            <el-option :label="$t('leave.type.annual')" value="ANNUAL" />
            <el-option :label="$t('leave.type.other')" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('leave.table.startTime')" prop="startTime">
          <el-date-picker
            v-model="temp.startTime"
            type="datetime"
            :placeholder="$t('common.pleaseSelect')"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item :label="$t('leave.table.endTime')" prop="endTime">
          <el-date-picker
            v-model="temp.endTime"
            type="datetime"
            :placeholder="$t('common.pleaseSelect')"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item :label="$t('leave.table.reason')" prop="reason">
          <el-input
            v-model="temp.reason"
            :autosize="{ minRows: 2, maxRows: 4 }"
            type="textarea"
            :placeholder="$t('common.pleaseInput')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="createData">{{ $t('common.confirm') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getLeaveList,
  applyLeave,
  approveLeave,
  type LeaveRequest,
  type LeaveQueryDTO,
  type LeaveApplicationDTO
} from '@/api/attendance'
import { useUserStore } from '@/store/user'
import { useI18n } from 'vue-i18n'

const userStore = useUserStore()
const { t } = useI18n()
const list = ref<LeaveRequest[]>([])
const listLoading = ref(true)
const queryParams = reactive<LeaveQueryDTO>({
  techName: '',
  status: undefined,
  storeId: undefined
})

const dialogFormVisible = ref(false)
const dialogStatus = ref('create')

const textMap = computed<any>(() => ({
  create: t('leave.dialog.create')
}))

const temp = reactive<LeaveApplicationDTO>({
  storeId: 1,
  type: '',
  startTime: '',
  endTime: '',
  reason: ''
})

const dataFormRef = ref()

const rules = computed(() => ({
  type: [{ required: true, message: t('common.pleaseSelect'), trigger: 'change' }],
  startTime: [{ required: true, message: t('common.pleaseSelect'), trigger: 'change' }],
  endTime: [{ required: true, message: t('common.pleaseSelect'), trigger: 'change' }],
  reason: [{ required: true, message: t('common.pleaseInput'), trigger: 'blur' }]
}))

const statusFilter = (status: string) => {
  const statusMap: any = {
    APPROVED: 'success',
    PENDING: 'warning',
    REJECTED: 'danger'
  }
  return statusMap[status]
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    APPROVED: t('leave.filter.approved'),
    REJECTED: t('leave.filter.rejected'),
    PENDING: t('leave.filter.pending')
  }
  return map[status] || status
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

const getList = async () => {
  listLoading.value = true
  try {
    const response = await getLeaveList(queryParams)
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

const handleCreate = () => {
  temp.type = ''
  temp.startTime = ''
  temp.endTime = ''
  temp.reason = ''
  dialogStatus.value = 'create'
  dialogFormVisible.value = true
  nextTick(() => {
    dataFormRef.value?.clearValidate()
  })
}

const createData = () => {
  dataFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      try {
        await applyLeave(temp)
        dialogFormVisible.value = false
        ElMessage.success(t('leave.dialog.success'))
        getList()
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleExport = () => {
  const headers = [
    t('leave.table.id'),
    t('leave.table.techName'),
    t('leave.table.type'),
    t('leave.table.startTime'),
    t('leave.table.endTime'),
    t('leave.table.reason'),
    t('leave.table.status'),
    t('leave.table.createTime')
  ]
  const data = list.value.map((item) => [
    item.id,
    item.techName,
    getTypeText(item.type),
    item.startTime,
    item.endTime,
    item.reason,
    getStatusText(item.status || ''),
    item.createTime
  ])

  const csvContent = [headers.join(','), ...data.map((row) => row.join(','))].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'leave_requests.csv'
  link.click()
}

const handleApprove = (row: LeaveRequest) => {
  ElMessageBox.confirm(t('leave.dialog.approveConfirm'), t('common.operation'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  }).then(async () => {
    try {
      await approveLeave({ id: row.id!, status: 'APPROVED' })
      ElMessage.success(t('leave.dialog.approveSuccess'))
      getList()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleReject = (row: LeaveRequest) => {
  ElMessageBox.confirm(t('leave.dialog.rejectConfirm'), t('common.operation'), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  }).then(async () => {
    try {
      await approveLeave({ id: row.id!, status: 'REJECTED' })
      ElMessage.success(t('leave.dialog.rejectSuccess'))
      getList()
    } catch (error) {
      console.error(error)
    }
  })
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
