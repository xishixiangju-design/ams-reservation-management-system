<template>
  <el-container class="layout-container">
    <el-aside width="220px">
      <div class="logo" @click="router.push('/admin/dashboard')">
        <h2>AMS Admin</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <template v-if="isAdmin">
          <el-menu-item index="/admin/service">
            <el-icon><Service /></el-icon>
            <span>{{ $t('menu.service') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/tech-mgt" @click="router.push('/admin/tech-mgt')">
            <el-icon><User /></el-icon>
            <span>{{ $t('menu.technician') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/room">
            <el-icon><House /></el-icon>
            <span>{{ $t('menu.room') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/appointment">
            <el-icon><Calendar /></el-icon>
            <span>{{ $t('menu.appointment') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/customer">
            <el-icon><Avatar /></el-icon>
            <span>{{ $t('menu.customer') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/waiting-list">
            <el-icon><Timer /></el-icon>
            <span>{{ $t('menu.waitingList') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><Document /></el-icon>
            <span>{{ $t('menu.logs') }}</span>
          </el-menu-item>

          <el-sub-menu index="/admin/attendance">
            <template #title>
              <el-icon><Clock /></el-icon>
              <span>{{ $t('menu.attendance') }}</span>
            </template>
            <el-menu-item index="/admin/attendance/leave">{{
              $t('menu.leaveRequest')
            }}</el-menu-item>
            <el-menu-item index="/admin/attendance/record">{{
              $t('menu.recordManagement')
            }}</el-menu-item>
          </el-sub-menu>
        </template>

        <template v-if="isTechnician">
          <el-menu-item index="/admin/technician/my-appointments">
            <el-icon><Calendar /></el-icon>
            <span>{{ $t('menu.myAppointments') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/technician/leave">
            <el-icon><Timer /></el-icon>
            <span>{{ $t('menu.myLeave') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/technician/attendance">
            <el-icon><Clock /></el-icon>
            <span>{{ $t('menu.myAttendance') }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="breadcrumb">
            <!-- Breadcrumb can be added here -->
          </div>
          <div class="user-info">
            <LangSelect />
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                {{ userStore.userInfo.username || 'Admin' }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">{{ $t('common.logout') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import {
  Service,
  User,
  House,
  Calendar,
  ArrowDown,
  Avatar,
  Timer,
  Document,
  Clock
} from '@element-plus/icons-vue'
import LangSelect from '@/components/LangSelect.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const hasRoles = computed(() => userStore.userInfo.roles && userStore.userInfo.roles.length > 0)
const isAdminAuth = computed(
  () =>
    userStore.userInfo.roles?.includes('ADMIN') ||
    userStore.userInfo.roles?.includes('ROLE_ADMIN') ||
    userStore.userInfo.roles?.includes('ROLE_MANAGER')
)
const isAdmin = computed(() => !hasRoles.value || isAdminAuth.value)

const isTechnician = computed(
  () =>
    userStore.userInfo.roles?.includes('TECHNICIAN') ||
    userStore.userInfo.roles?.includes('ROLE_TECHNICIAN') ||
    userStore.userInfo.roles?.includes('ROLE_TECH')
)

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout().then(() => {
      router.push('/login')
    })
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
  color: #fff;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background-color: #2b3649;
  cursor: pointer;
}

.logo h2 {
  margin: 0;
  color: #fff;
  font-size: 20px;
}

.el-menu {
  border-right: none;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  color: #333;
  line-height: 60px;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
