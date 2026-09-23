<template>
  <div class="customer-container">
    <div class="toolbar">
      <el-input
        v-model="keyword"
        :placeholder="$t('customer.toolbar.placeholder')"
        style="width: 200px; margin-right: 10px"
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
      <el-button type="success" @click="handleAdd">{{ $t('customer.toolbar.add') }}</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" style="width: 100%">
      <el-table-column prop="id" :label="$t('customer.columns.id')" width="80" />
      <el-table-column prop="username" :label="$t('customer.columns.account')" />
      <el-table-column prop="nickname" :label="$t('customer.columns.nickname')" />
      <el-table-column prop="membershipLevel" :label="$t('customer.columns.level')">
        <template #default="scope">
          <el-tag v-if="scope.row.membershipLevel === 'GOLD'" type="warning">{{
            $t('customer.level.gold')
          }}</el-tag>
          <el-tag
            v-else-if="scope.row.membershipLevel === 'PLATINUM'"
            color="#E5EAF3"
            style="color: #333; border-color: #dcdfe6"
            >{{ $t('customer.level.platinum') }}</el-tag
          >
          <el-tag v-else type="info">{{ $t('customer.level.normal') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="discountRate" :label="$t('customer.columns.discount')">
        <template #default="scope">
          {{ scope.row.discountRate ? scope.row.discountRate : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="violationCount" :label="$t('customer.columns.violation')" width="100">
        <template #default="scope">
          <span
            :style="{
              color: (scope.row.violationCount || 0) > 0 ? 'red' : 'inherit',
              fontWeight: (scope.row.violationCount || 0) > 0 ? 'bold' : 'normal'
            }"
          >
            {{ scope.row.violationCount || 0 }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" :label="$t('customer.columns.regTime')" width="180" />
      <el-table-column :label="$t('common.operation')" width="150">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handleEdit(scope.row)">{{
            $t('common.edit')
          }}</el-button>
          <el-popconfirm :title="$t('common.delete') + '?'" @confirm="handleDelete(scope.row)">
            <template #reference>
              <el-button size="small" type="danger">{{ $t('common.delete') }}</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        layout="total, prev, pager, next"
        :total="total"
        size="small"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- Add Dialog -->
    <el-dialog v-model="addDialogVisible" :title="$t('customer.dialog.addTitle')" width="400px">
      <el-form ref="addFormRef" :model="addFormData" :rules="addRules" label-width="100px">
        <el-form-item :label="$t('customer.dialog.account')" prop="username">
          <el-input v-model="addFormData.username" :placeholder="$t('common.pleaseInput')" />
        </el-form-item>
        <el-form-item :label="$t('customer.dialog.password')" prop="password">
          <el-input
            v-model="addFormData.password"
            type="password"
            :placeholder="$t('common.pleaseInput')"
            show-password
          />
        </el-form-item>
        <el-form-item :label="$t('customer.dialog.nickname')" prop="nickname">
          <el-input v-model="addFormData.nickname" :placeholder="$t('common.pleaseInput')" />
        </el-form-item>
        <el-form-item :label="$t('customer.dialog.email')" prop="email">
          <el-input v-model="addFormData.email" :placeholder="$t('common.pleaseInput')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleAddSubmit">{{ $t('common.confirm') }}</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="$t('customer.dialog.editTitle')" width="400px">
      <el-form :model="formData" label-width="100px">
        <el-form-item :label="$t('customer.dialog.level')">
          <el-select v-model="formData.membershipLevel" :placeholder="$t('common.pleaseSelect')">
            <el-option :label="$t('customer.level.normal')" value="NORMAL" />
            <el-option :label="$t('customer.level.gold')" value="GOLD" />
            <el-option :label="$t('customer.level.platinum')" value="PLATINUM" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('customer.dialog.discount')">
          <el-input-number
            v-model="formData.discountRate"
            :precision="2"
            :step="0.05"
            :max="1.0"
            :min="0.1"
          />
          <div class="tips">{{ $t('customer.dialog.discountTip') }}</div>
        </el-form-item>
        <el-form-item :label="$t('customer.dialog.violation')">
          <el-input-number v-model="formData.violationCount" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSubmit">{{ $t('common.save') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, nextTick } from 'vue'
import {
  getCustomerList,
  updateCustomer,
  addCustomer,
  deleteCustomer,
  type CustomerDTO
} from '@/api/customer'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const loading = ref(false)
const tableData = ref<CustomerDTO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const addDialogVisible = ref(false)
const addFormRef = ref<FormInstance>()
const addFormData = reactive({
  username: '',
  password: '',
  nickname: '',
  email: ''
})
const addRules = reactive<FormRules>({
  username: [{ required: true, message: t('login.rules.account'), trigger: 'blur' }],
  password: [{ required: true, message: t('login.rules.password'), trigger: 'blur' }],
  nickname: [{ required: true, message: t('register.rules.nickname'), trigger: 'blur' }]
})

const handleAdd = () => {
  addFormData.username = ''
  addFormData.password = ''
  addFormData.nickname = ''
  addFormData.email = ''
  addDialogVisible.value = true
  nextTick(() => {
    addFormRef.value?.clearValidate()
  })
}

const handleAddSubmit = async () => {
  if (!addFormRef.value) return
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await addCustomer(addFormData)
        ElMessage.success(t('common.success'))
        addDialogVisible.value = false
        fetchData()
      } catch (e) {
        // error
      }
    }
  })
}

const dialogVisible = ref(false)
const formData = reactive<Partial<CustomerDTO>>({
  id: 0,
  membershipLevel: '',
  discountRate: undefined,
  violationCount: 0
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getCustomerList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value
    })
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchData()
}

const handleCurrentChange = (val: number) => {
  pageNum.value = val
  fetchData()
}

const handleEdit = (row: CustomerDTO) => {
  formData.id = row.id
  formData.membershipLevel = row.membershipLevel || 'NORMAL'
  formData.discountRate = row.discountRate
  formData.violationCount = row.violationCount || 0
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await updateCustomer(formData.id!, {
      membershipLevel: formData.membershipLevel,
      discountRate: formData.discountRate,
      violationCount: formData.violationCount
    })
    ElMessage.success('更新成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    // error
  }
}

const handleDelete = async (row: CustomerDTO) => {
  try {
    await deleteCustomer(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.customer-container {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.tips {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style>
