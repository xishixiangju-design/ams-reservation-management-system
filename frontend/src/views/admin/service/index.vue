<template>
  <div class="service-container">
    <el-card>
      <div class="toolbar mb-4 flex justify-between">
        <span class="text-lg font-bold">{{ $t('menu.service') }}</span>
        <el-button type="primary" @click="handleAdd">{{ $t('service.add') }}</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="id" :label="$t('service.columns.id')" width="80" align="center" />
        <el-table-column prop="name" :label="$t('service.columns.name')" />
        <el-table-column
          prop="description"
          :label="$t('service.columns.desc')"
          show-overflow-tooltip
        />
        <el-table-column prop="price" :label="$t('service.columns.price')" align="center">
          <template #default="scope"> ¥{{ scope.row.price }} </template>
        </el-table-column>
        <el-table-column prop="duration" :label="$t('service.columns.duration')" align="center" />
        <el-table-column prop="status" :label="$t('service.columns.status')" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? $t('service.status.on') : $t('service.status.off') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="200" align="center">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">{{
              $t('common.edit')
            }}</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">{{
              $t('common.delete')
            }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? $t('service.dialog.addTitle') : $t('service.dialog.editTitle')"
      width="500px"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item :label="$t('service.dialog.name')" prop="name">
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item :label="$t('service.dialog.desc')" prop="description">
          <el-input v-model="formData.description" type="textarea" />
        </el-form-item>
        <el-form-item :label="$t('service.dialog.price')" prop="price">
          <el-input-number v-model="formData.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item :label="$t('service.dialog.duration')" prop="duration">
          <el-input-number v-model="formData.duration" :min="1" :step="15" />
        </el-form-item>
        <el-form-item :label="$t('service.dialog.status')" prop="status">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            :active-text="$t('service.status.on')"
            :inactive-text="$t('service.status.off')"
          />
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
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getServiceList, createService, updateService, deleteService } from '@/api/service'
import type { ServiceDTO } from '@/api/service'
import type { FormInstance } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const loading = ref(false)
const tableData = ref<ServiceDTO[]>([])
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()

const formData = reactive<ServiceDTO>({
  name: '',
  description: '',
  price: 0,
  duration: 60,
  status: 1
})

const rules = reactive<FormInstance['rules']>({
  name: [
    { required: true, message: t('common.pleaseInput') + t('service.dialog.name'), trigger: 'blur' }
  ],
  price: [
    {
      required: true,
      message: t('common.pleaseInput') + t('service.dialog.price'),
      trigger: 'blur'
    }
  ],
  duration: [
    {
      required: true,
      message: t('common.pleaseInput') + t('service.dialog.duration'),
      trigger: 'blur'
    }
  ]
})

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getServiceList()
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogType.value = 'add'
  formData.id = undefined
  formData.name = ''
  formData.description = ''
  formData.price = 0
  formData.duration = 60
  formData.status = 1
  dialogVisible.value = true
}

const handleEdit = (row: ServiceDTO) => {
  dialogType.value = 'edit'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = (row: ServiceDTO) => {
  ElMessageBox.confirm(t('common.delete') + '?', t('common.confirm'), {
    type: 'warning'
  }).then(async () => {
    await deleteService(row.id!)
    ElMessage.success(t('common.success'))
    loadData()
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (dialogType.value === 'add') {
        await createService(formData)
        ElMessage.success(t('common.success'))
      } else {
        await updateService(formData.id!, formData)
        ElMessage.success(t('common.success'))
      }
      dialogVisible.value = false
      loadData()
    }
  })
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

<script>
window.difyChatbotConfig = {
  token: 'Dh6xHTXjXbUG7EVs',
  inputs: {
    // You can define the inputs from the Start node here
    // key is the variable name
    // e.g.
    // name: "NAME"
  },
  systemVariables: {
    // user_id: 'YOU CAN DEFINE USER ID HERE',
    // conversation_id: 'YOU CAN DEFINE CONVERSATION ID HERE, IT MUST BE A VALID UUID',
  },
  userVariables: {
    // avatar_url: 'YOU CAN DEFINE USER AVATAR URL HERE',
    // name: 'YOU CAN DEFINE USER NAME HERE',
  },
}
</script>
<script
  src="https://udify.app/embed.min.js"
  id="Dh6xHTXjXbUG7EVs"
  defer>
</script>
<style>
#dify-chatbot-bubble-button {
  background-color: #1C64F2 !important;
}
#dify-chatbot-bubble-window {
  width: 24rem !important;
  height: 40rem !important;
}
</style>