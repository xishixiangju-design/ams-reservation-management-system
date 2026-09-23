<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>{{ $t('login.title') }}</span>
          <LangSelect />
        </div>
      </template>
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-position="top">
        <el-form-item :label="$t('login.account')" prop="username">
          <el-input v-model="loginForm.username" :placeholder="$t('login.placeholder.account')" />
        </el-form-item>
        <el-form-item :label="$t('login.password')" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            show-password
            :placeholder="$t('login.placeholder.password')"
          />
        </el-form-item>
        <el-form-item :label="$t('login.captcha')" prop="userInputCaptcha">
          <div class="flex items-center w-full">
            <el-input
              v-model="loginForm.userInputCaptcha"
              :placeholder="$t('login.placeholder.captcha')"
              class="flex-1 mr-2"
            />
            <img
              v-if="captchaUrl"
              :src="captchaUrl"
              class="cursor-pointer h-8"
              alt="captcha"
              title="Click to refresh"
              style="height: 32px; border: 1px solid #dcdfe6; border-radius: 4px"
              @click="refreshCaptcha"
            />
          </div>
        </el-form-item>
        <div class="flex justify-between items-center mb-4">
          <el-checkbox v-model="rememberMe">{{ $t('login.rememberMe') }}</el-checkbox>
          <el-link type="primary" underline="never" @click="router.push('/reset-password')">{{
            $t('login.forgotPassword')
          }}</el-link>
        </div>
        <el-form-item>
          <el-button type="primary" class="w-full" :loading="loading" @click="handleLogin">{{
            loading ? $t('login.loggingIn') : $t('login.loginBtn')
          }}</el-button>
        </el-form-item>
        <div class="text-center">
          <router-link to="/register" class="text-sm text-blue-500">{{
            $t('login.register')
          }}</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getCaptcha } from '@/api/auth'
import { getRedirectPath } from '@/utils/redirect'
import LangSelect from '@/components/LangSelect.vue'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const { t } = useI18n()

const loginForm = reactive({
  username: '',
  password: '',
  userInputCaptcha: '',
  captchaUuid: ''
})

const captchaUrl = ref('')

const refreshCaptcha = async () => {
  try {
    const res: any = await getCaptcha()
    if (res.code === 200) {
      captchaUrl.value = res.data.image
      loginForm.captchaUuid = res.data.uuid
    }
  } catch (error) {
    console.error(error)
  }
}

const rememberMe = ref(false)
const loading = ref(false)

const rules = computed<FormRules>(() => ({
  username: [{ required: true, message: t('login.rules.account'), trigger: 'blur' }],
  password: [{ required: true, message: t('login.rules.password'), trigger: 'blur' }],
  userInputCaptcha: [{ required: true, message: t('login.rules.captcha'), trigger: 'blur' }]
}))

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm)
        ElMessage.success(t('login.success'))

        // Role-based redirection logic
        const roles = userStore.userInfo.roles || []
        const redirectPath = getRedirectPath(roles)
        router.replace(redirectPath)
      } catch (error) {
        refreshCaptcha()
        loginForm.userInputCaptcha = ''
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-card {
  width: 100%;
  max-width: 400px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.w-full {
  width: 100%;
}
.flex {
  display: flex;
}
.justify-between {
  justify-content: space-between;
}
.items-center {
  align-items: center;
}
.mb-4 {
  margin-bottom: 1rem;
}
.text-center {
  text-align: center;
}
.text-sm {
  font-size: 0.875rem;
}
.text-blue-500 {
  color: #409eff;
  text-decoration: none;
}
</style>
