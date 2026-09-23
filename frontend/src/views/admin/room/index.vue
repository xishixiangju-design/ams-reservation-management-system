<template>
  <div class="room-container">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增房间</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="房间名称" />
      <el-table-column prop="type" label="类型">
        <template #default="scope">
          <el-tag :type="scope.row.type === 'SINGLE' ? '' : 'success'">
            {{ scope.row.type === 'SINGLE' ? '单人间' : '双人间' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="capacity" label="容量" />
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="handleEdit(scope.row)"
            >编辑</el-button
          >
          <el-button link type="danger" size="small" @click="handleDelete(scope.row)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增房间' : '编辑房间'"
      width="500px"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="房间名称" prop="name">
          <el-input v-model="formData.name" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型">
            <el-option label="单人间" value="SINGLE" />
            <el-option label="双人间" value="DOUBLE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态">
            <el-option label="空闲" :value="1" />
            <el-option label="使用中" :value="2" />
            <el-option label="维护中" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoomList, createRoom, updateRoom, deleteRoom } from '@/api/room'
import type { RoomDTO } from '@/api/room'
import type { FormInstance } from 'element-plus'

const loading = ref(false)
const tableData = ref<RoomDTO[]>([])
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()

const formData = reactive<RoomDTO>({
  id: undefined,
  name: '',
  type: 'SINGLE',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入房间名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const getStatusType = (status: number): 'success' | 'warning' | 'info' | '' => {
  switch (status) {
    case 1:
      return 'success'
    case 2:
      return 'warning'
    case 3:
      return 'info'
    default:
      return ''
  }
}

const getStatusText = (status: number) => {
  switch (status) {
    case 1:
      return '空闲'
    case 2:
      return '使用中'
    case 3:
      return '维护中'
    default:
      return '未知'
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getRoomList()
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogType.value = 'add'
  formData.id = undefined
  formData.name = ''
  formData.type = 'SINGLE'
  formData.status = 1
  dialogVisible.value = true
}

const handleEdit = (row: RoomDTO) => {
  dialogType.value = 'edit'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = (row: RoomDTO) => {
  ElMessageBox.confirm('确认删除该房间吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteRoom(row.id!)
    ElMessage.success(t('common.success'))
    loadData()
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (dialogType.value === 'add') {
        await createRoom(formData)
        ElMessage.success(t('common.success'))
      } else {
        if (!formData.id) {
          ElMessage.error('房间ID不存在，无法更新')
          return
        }
        await updateRoom(formData.id, formData)
        ElMessage.success('操作成功')
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
