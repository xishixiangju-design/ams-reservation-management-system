<template>
  <div class="app-container">
    <el-card class="filter-container" shadow="never">
      <el-form :inline="true" :model="queryParams" size="default">
        <el-form-item :label="$t('logs.module')">
          <el-input
            v-model="queryParams.module"
            :placeholder="$t('logs.module')"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item :label="$t('logs.operator')">
          <el-input
            v-model="queryParams.username"
            :placeholder="$t('logs.operator')"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item :label="$t('logs.status')">
          <el-select
            v-model="queryParams.status"
            :placeholder="$t('logs.status')"
            clearable
            style="width: 120px"
          >
            <el-option :label="$t('logs.success')" :value="0" />
            <el-option :label="$t('logs.fail')" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">{{
            $t('common.search')
          }}</el-button>
          <el-button :icon="Refresh" @click="resetQuery">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-container" shadow="never" style="margin-top: 20px">
      <el-table v-loading="loading" :data="logList" border style="width: 100%">
        <el-table-column prop="id" :label="$t('logs.columns.id')" width="80" align="center" />
        <el-table-column prop="module" :label="$t('logs.columns.module')" width="120" />
        <el-table-column
          prop="businessType"
          :label="$t('logs.columns.type')"
          width="150"
          show-overflow-tooltip
        />
        <el-table-column prop="username" :label="$t('logs.columns.operator')" width="120" />
        <el-table-column prop="ipAddr" :label="$t('logs.columns.ip')" width="140" />
        <el-table-column prop="operTime" :label="$t('logs.columns.time')" width="180" />
        <el-table-column
          prop="status"
          :label="$t('logs.columns.status')"
          width="100"
          align="center"
        >
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
              {{ scope.row.status === 0 ? $t('logs.success') : $t('logs.fail') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="costTime"
          :label="$t('logs.columns.cost')"
          width="100"
          align="center"
        />
        <el-table-column
          prop="errorMsg"
          :label="$t('logs.columns.error')"
          min-width="200"
          show-overflow-tooltip
        />
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; text-align: right">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getOperationLogs } from '@/api/logs'
import type { LogQuery, LogDTO } from '@/api/logs'

const loading = ref(false)
const total = ref(0)
const logList = ref<LogDTO[]>([])

const queryParams = reactive<LogQuery>({
  pageNum: 1,
  pageSize: 10,
  module: '',
  username: '',
  status: undefined
})

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await getOperationLogs(queryParams)
    logList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.module = ''
  queryParams.username = ''
  queryParams.status = undefined
  queryParams.pageNum = 1
  handleQuery()
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.filter-container {
  margin-bottom: 20px;
}
</style>
