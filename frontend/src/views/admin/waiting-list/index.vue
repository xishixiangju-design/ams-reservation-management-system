<template>
  <div class="app-container">
    <el-card class="filter-container" shadow="never">
      <el-form :inline="true" :model="queryParams" size="default">
        <el-form-item :label="$t('waitingList.filter.keyword')">
          <el-input
            v-model="queryParams.keyword"
            :placeholder="$t('appointment.list.keywordPlaceholder')"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item :label="$t('waitingList.filter.status')">
          <el-select
            v-model="queryParams.status"
            :placeholder="$t('appointment.list.status.all')"
            clearable
            style="width: 120px"
          >
            <el-option :label="$t('waitingList.status.waiting')" value="WAITING" />
            <el-option :label="$t('waitingList.status.notified')" value="NOTIFIED" />
            <el-option :label="$t('waitingList.status.converted')" value="CONVERTED" />
            <el-option :label="$t('waitingList.status.expired')" value="EXPIRED" />
            <el-option :label="$t('waitingList.status.cancelled')" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('waitingList.filter.date')">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            :range-separator="$t('common.to')"
            :start-placeholder="$t('common.startTime')"
            :end-placeholder="$t('common.endTime')"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
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
      <el-table v-loading="loading" :data="waitingList" border style="width: 100%">
        <el-table-column
          prop="id"
          :label="$t('waitingList.columns.id')"
          width="80"
          align="center"
        />
        <el-table-column
          prop="customerName"
          :label="$t('waitingList.columns.customer')"
          width="120"
        />
        <el-table-column
          prop="customerPhone"
          :label="$t('waitingList.columns.phone')"
          width="120"
        />
        <el-table-column
          prop="serviceName"
          :label="$t('waitingList.columns.service')"
          min-width="150"
          show-overflow-tooltip
        />
        <el-table-column prop="techName" :label="$t('waitingList.columns.tech')" width="120">
          <template #default="scope">
            {{ scope.row.techName || $t('waitingList.columns.anyTech') }}
          </template>
        </el-table-column>
        <el-table-column
          prop="expectedDate"
          :label="$t('waitingList.columns.date')"
          width="120"
          sortable
        />
        <el-table-column prop="timeRange" :label="$t('waitingList.columns.time')" width="120" />
        <el-table-column
          prop="peopleCount"
          :label="$t('waitingList.columns.people')"
          width="80"
          align="center"
        />
        <el-table-column
          prop="status"
          :label="$t('waitingList.columns.status')"
          width="100"
          align="center"
        >
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{
              getStatusLabel(scope.row.status)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          :label="$t('waitingList.columns.joinTime')"
          width="160"
        />
        <el-table-column :label="$t('common.operation')" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-popconfirm
              v-if="scope.row.status === 'WAITING' || scope.row.status === 'NOTIFIED'"
              :title="$t('waitingList.convertConfirm')"
              @confirm="handleConvert(scope.row)"
            >
              <template #reference>
                <el-button type="primary" link size="small">{{
                  $t('waitingList.convert')
                }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
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
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getAdminWaitingList, convertWaitingList } from '@/api/waitingList'
import type { WaitingListQuery, WaitingListDTO } from '@/api/waitingList'

const { t } = useI18n()
const loading = ref(false)
const total = ref(0)
const waitingList = ref<WaitingListDTO[]>([])
const dateRange = ref<[string, string] | null>(null)

const queryParams = reactive<WaitingListQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: undefined,
  startDate: undefined,
  endDate: undefined
})

const getStatusType = (status: string): 'success' | 'info' | 'warning' | 'danger' => {
  const map = {
    WAITING: 'warning',
    NOTIFIED: 'info',
    CONVERTED: 'success',
    EXPIRED: 'danger',
    CANCELLED: 'info'
  } as const
  return map[status as keyof typeof map] ?? 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    WAITING: t('waitingList.status.waiting'),
    NOTIFIED: t('waitingList.status.notified'),
    CONVERTED: t('waitingList.status.converted'),
    EXPIRED: t('waitingList.status.expired'),
    CANCELLED: t('waitingList.status.cancelled')
  }
  return map[status] || status
}

const handleDateChange = (val: [string, string] | null) => {
  if (val) {
    queryParams.startDate = val[0]
    queryParams.endDate = val[1]
  } else {
    queryParams.startDate = undefined
    queryParams.endDate = undefined
  }
  handleQuery()
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await getAdminWaitingList(queryParams)
    waitingList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.status = undefined
  queryParams.startDate = undefined
  queryParams.endDate = undefined
  dateRange.value = null
  queryParams.pageNum = 1
  handleQuery()
}

const handleConvert = async (row: WaitingListDTO) => {
  try {
    await convertWaitingList(row.id)
    ElMessage.success(t('common.success'))
    handleQuery()
  } catch (error) {
    console.error(error)
  }
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
