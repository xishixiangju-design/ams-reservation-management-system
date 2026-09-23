<template>
  <div class="notification-container">
    <div class="header">
      <span>{{ $t('notification.title') }}</span>
      <el-button link size="small" @click="handleMarkAllRead">{{
        $t('notification.markAll')
      }}</el-button>
    </div>

    <div v-loading="loading" class="list">
      <el-empty v-if="!loading && list.length === 0" :description="$t('notification.empty')" />

      <div
        v-for="item in list"
        :key="item.id"
        class="notification-item"
        :class="{ unread: item.readStatus === 0 }"
        @click="handleRead(item)"
      >
        <div class="item-header">
          <span class="title">{{ item.title }}</span>
          <span class="time">{{ formatTime(item.createTime) }}</span>
        </div>
        <div class="content">{{ item.content }}</div>
        <div class="actions">
          <el-button link size="small" @click.stop="handleDelete(item)">{{
            $t('common.delete')
          }}</el-button>
        </div>
      </div>
    </div>

    <div v-if="total > 0" class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        size="small"
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  getMyNotifications,
  markAsRead,
  markAllAsRead,
  deleteNotification,
  type Notification
} from '@/api/notification'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const emit = defineEmits(['update-unread'])

const loading = ref(false)
const list = ref<Notification[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(5)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyNotifications({ page: pageNum.value, size: pageSize.value })
    if (res.data) {
      list.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleRead = async (item: Notification) => {
  if (item.readStatus === 0) {
    await markAsRead(item.id)
    item.readStatus = 1
    emit('update-unread')
  }
}

const handleMarkAllRead = async () => {
  await markAllAsRead()
  list.value.forEach((i) => (i.readStatus = 1))
  emit('update-unread')
}

const handleDelete = async (item: Notification) => {
  await deleteNotification(item.id)
  loadData()
  emit('update-unread')
}

const formatTime = (time: string) => {
  return dayjs(time).format('MM-DD HH:mm')
}

onMounted(() => {
  loadData()
})

defineExpose({ loadData })
</script>

<style scoped>
.notification-container {
  max-height: 400px;
  display: flex;
  flex-direction: column;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
  margin-bottom: 10px;
}
.list {
  flex: 1;
  overflow-y: auto;
}
.notification-item {
  padding: 10px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.3s;
}
.notification-item:hover {
  background: #f9f9f9;
}
.notification-item.unread .title {
  font-weight: bold;
  color: #303133;
}
.notification-item.unread::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #f56c6c;
  border-radius: 50%;
  margin-right: 5px;
  vertical-align: middle;
}
.item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}
.time {
  font-size: 12px;
  color: #909399;
}
.content {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
}
.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 5px;
}
.pagination {
  margin-top: 10px;
  text-align: center;
}
</style>
