<template>
  <div class="booking-layout">
    <div class="top-nav">
      <div class="nav-left">
        <span class="logo">Kasa AMS</span>
      </div>
      <div class="nav-right">
        <LangSelect />
        <el-popover placement="bottom" :width="350" trigger="click" @show="handlePopoverShow">
          <template #reference>
            <el-badge
              :value="unreadCount"
              :hidden="unreadCount === 0"
              class="item"
              style="margin-right: 20px; cursor: pointer"
            >
              <el-icon :size="20" style="vertical-align: middle; color: #409eff"><Bell /></el-icon>
            </el-badge>
          </template>
          <NotificationList ref="notificationRef" @update-unread="fetchUnreadCount" />
        </el-popover>

        <el-button link type="primary" @click="$router.push('/booking')">{{
          $t('booking.title')
        }}</el-button>
        <el-button link type="primary" @click="$router.push('/my-appointments')">{{
          $t('booking.myAppointments')
        }}</el-button>
        <el-button link type="primary" @click="$router.push('/member-center')">{{
          $t('booking.memberCenter')
        }}</el-button>
        <el-button link type="primary" @click="handleLogout">{{ $t('common.logout') }}</el-button>
      </div>
    </div>

    <div v-if="showSteps" class="header">
      <h2>{{ $t('booking.title') }}</h2>
      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step :title="$t('booking.step1')" />
        <el-step :title="$t('booking.step2')" />
        <el-step :title="$t('booking.step3')" />
      </el-steps>
    </div>
    <div class="content">
      <router-view />
    </div>

    <!-- Dify AI 助手悬浮按钮 -->
    <DifyChatbot />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import NotificationList from '@/components/NotificationList.vue'
import { getUnreadCount } from '@/api/notification'
import LangSelect from '@/components/LangSelect.vue'
import DifyChatbot from '@/components/DifyChatbot.vue'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { t } = useI18n()

const unreadCount = ref(0)
const notificationRef = ref()

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data
  } catch (e) {
    // ignore
  }
}

const handlePopoverShow = () => {
  notificationRef.value?.loadData()
}

onMounted(() => {
  fetchUnreadCount()
})

const activeStep = computed(() => {
  if (route.name === 'booking-service') return 0
  if (route.name === 'booking-tech') return 1
  if (route.name === 'booking-confirm') return 2
  if (route.name === 'booking-success') return 3
  return 0
})

const showSteps = computed(() => {
  return route.path.startsWith('/booking') && route.name !== 'booking-success'
})

const handleLogout = async () => {
  try {
    await userStore.logout()
    ElMessage.success(t('booking.logoutSuccess'))
    router.push('/login')
  } catch (error) {
    // ignore
    router.push('/login')
  }
}
</script>

<style scoped>
.booking-layout {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.header {
  margin-bottom: 30px;
}
.content {
  background: #fff;
  min-height: 400px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

@media (max-width: 414px) {
  .booking-layout {
    padding: 10px;
  }
  .content {
    padding: 10px;
  }
  .top-nav {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
