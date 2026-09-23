<template>
  <div class="technician-container">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">{{ $t('technician.add') }}</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" style="width: 100%">
      <el-table-column prop="userId" :label="$t('technician.columns.id')" width="80" />
      <el-table-column prop="realName" :label="$t('technician.columns.name')" />
      <el-table-column :label="$t('technician.columns.account')">
        <template #default="scope">
          {{ scope.row.sysUser?.username }}
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="$t('technician.columns.status')">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.operation')" width="280">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="handleEdit(scope.row)">
            {{ $t('common.edit') }}
          </el-button>
          <el-button link type="danger" size="small" @click="handleDelete(scope.row)">
            {{ $t('common.delete') }}
          </el-button>
          <el-divider direction="vertical" />
          <el-button-group>
            <el-button
              size="small"
              :disabled="scope.row.status === 'IDLE'"
              @click="handleStatusChange(scope.row, 'IDLE')"
              >{{ $t('technician.status.idle') }}</el-button
            >
            <el-button
              size="small"
              type="warning"
              :disabled="scope.row.status === 'BUSY'"
              @click="handleStatusChange(scope.row, 'BUSY')"
              >{{ $t('technician.status.busy') }}</el-button
            >
            <el-button
              size="small"
              type="info"
              :disabled="scope.row.status === 'LEAVE'"
              @click="handleStatusChange(scope.row, 'LEAVE')"
              >{{ $t('technician.status.leave') }}</el-button
            >
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- Conflict Dialog -->
    <el-dialog
      v-model="conflictDialogVisible"
      :title="$t('technician.dialog.conflictTitle')"
      width="600px"
    >
      <p style="color: #f56c6c; margin-bottom: 10px">
        {{ $t('technician.dialog.conflictMsg') }}
      </p>
      <el-table :data="conflictList" border size="small">
        <el-table-column prop="apptId" :label="$t('appointment.detail.orderNo')" width="80" />
        <el-table-column prop="startTime" :label="$t('common.startTime')" width="160">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="customerName"
          :label="$t('appointment.create.confirm.customer')"
          width="100"
        />
        <el-table-column prop="serviceName" :label="$t('appointment.create.service.name')" />
        <el-table-column :label="$t('common.operation')" width="80">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="goToAppointment(row.apptId)">
              {{ $t('common.operation') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="conflictDialogVisible = false">{{ $t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? $t('common.edit') : $t('technician.add')"
      width="500px"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item :label="$t('technician.dialog.name')" prop="realName">
          <el-input v-model="formData.realName" />
        </el-form-item>
        <el-form-item :label="$t('technician.dialog.account')" prop="username">
          <el-input v-model="formData.username" :placeholder="$t('login.placeholder.account')" />
        </el-form-item>
        <el-form-item :label="$t('technician.dialog.password')" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            show-password
            :placeholder="isEdit ? $t('technician.dialog.passwordPlaceholderEdit') : ''"
          />
        </el-form-item>
        <el-form-item :label="$t('technician.columns.level')" prop="level">
          <el-select v-model="formData.level" :placeholder="$t('common.pleaseSelect')">
            <el-option :label="$t('technician.levels.director')" value="Director" />
            <el-option :label="$t('technician.levels.senior')" value="Senior" />
            <el-option :label="$t('technician.levels.junior')" value="Junior" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('technician.dialog.introCn')" prop="introCn">
          <el-input v-model="formData.introCn" type="textarea" />
        </el-form-item>
        <el-form-item :label="$t('technician.dialog.introJp')" prop="introJp">
          <el-input v-model="formData.introJp" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  getTechnicianList,
  createTechnician,
  updateTechnician,
  updateTechnicianStatus,
  deleteTechnician,
  getTechnicianStats
} from '@/api/technician'
import type { TechnicianDTO } from '@/api/technician'
import type { FormInstance, FormRules } from 'element-plus'
import dayjs from 'dayjs'

const { t } = useI18n()
const router = useRouter()
const loading = ref(false)
const tableData = ref<TechnicianDTO[]>([])
const dialogVisible = ref(false)
const conflictDialogVisible = ref(false)
const conflictList = ref<any[]>([])
const formRef = ref<FormInstance>()
const isEdit = ref(false)

const formData = reactive<TechnicianDTO>({
  userId: undefined,
  realName: '',
  username: '',
  password: '',
  status: 'IDLE',
  level: '',
  introCn: '',
  introJp: ''
})

const validatePassword = (rule: any, value: any, callback: any) => {
  if (!isEdit.value && !value) {
    callback(new Error(t('login.rules.password')))
  } else {
    callback()
  }
}

const rules = reactive<FormRules>({
  realName: [
    {
      required: true,
      message: t('common.pleaseInput') + t('technician.dialog.name'),
      trigger: 'blur'
    }
  ],
  username: [{ required: true, message: t('login.rules.account'), trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }]
})

const formatTime = (time: string) => {
  return dayjs(time).format('MM-DD HH:mm')
}

const goToAppointment = (apptId: number) => {
  conflictDialogVisible.value = false
  // 跳转到预约管理页，并带上搜索参数
  router.push({
    path: '/admin/appointment',
    query: { id: apptId }
  })
}

const getStatusType = (status: string): 'success' | 'warning' | 'info' | undefined => {
  switch (status) {
    case 'IDLE':
      return 'success'
    case 'BUSY':
      return 'warning'
    case 'LEAVE':
      return 'info'
    default:
      return undefined
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'IDLE':
      return t('technician.status.idle')
    case 'BUSY':
      return t('technician.status.busy')
    case 'LEAVE':
      return t('technician.status.leave')
    default:
      return t('common.unknown')
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTechnicianList()
    tableData.value = res.data
  } catch (error) {
    console.error('Failed to load technician list:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  formData.userId = undefined
  formData.realName = ''
  formData.username = ''
  formData.password = ''
  formData.status = 'IDLE'
  formData.level = ''
  formData.introCn = ''
  formData.introJp = ''
  dialogVisible.value = true
}

const handleEdit = (row: TechnicianDTO) => {
  isEdit.value = true
  formData.userId = row.userId
  formData.realName = row.realName
  formData.username = row.sysUser?.username || ''
  formData.password = ''
  formData.status = row.status
  formData.level = row.level
  formData.introCn = row.introCn
  formData.introJp = row.introJp
  dialogVisible.value = true
}

const handleDelete = async (row: TechnicianDTO) => {
  try {
    const res = await getTechnicianStats(row.userId!)
    const stats = res.data

    await ElMessageBox.confirm(
      `${t('common.deleteConfirm')} ${row.realName}? 
      ${t('technician.columns.activeOrders')}: ${stats.activeOrderCount}
      ${t('technician.columns.totalOrders')}: ${stats.totalOrderCount}`,
      t('common.warning'),
      {
        confirmButtonText: t('common.delete'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )

    await deleteTechnician(row.userId!)
    ElMessage.success(t('common.success'))
    loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await updateTechnician(formData.userId!, formData)
    } else {
      await createTechnician(formData)
    }
    ElMessage.success(t('common.success'))
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('Failed to submit form:', error)
  }
}

const handleStatusChange = async (row: TechnicianDTO, status: string) => {
  try {
    await updateTechnicianStatus(row.userId!, status)
    ElMessage.success(t('common.success'))
    loadData()
  } catch (e: any) {
    // Check if it is a conflict error (code 409 or HTTP 409)
    const isConflict = e.code === 409 || e.response?.status === 409
    const conflictData = e.data || e.response?.data

    if (isConflict && conflictData) {
      conflictList.value = Array.isArray(conflictData) ? conflictData : []
      conflictDialogVisible.value = true
    } else {
      console.error('Failed to update status:', e)
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
