<template>
  <div class="reset-container">
    <el-card class="reset-card">
      <template #header>
        <div class="card-header">
          <span>{{ $t('login.resetPassword') }}</span>
          <LangSelect />
        </div>
      </template>
      <el-form ref="resetFormRef" :model="resetForm" :rules="rules" label-position="top">
        <el-form-item :label="$t('login.email')" prop="email">
          <el-input v-model="resetForm.email" :placeholder="$t('login.placeholder.email')" />
        </el-form-item>

        <el-form-item :label="$t('login.verificationCode')" prop="code">
          <div class="flex items-center w-full">
            <el-input
              v-model="resetForm.code"
              :placeholder="$t('login.placeholder.verificationCode')"
              class="flex-1 mr-2"
            />
            <el-button type="primary" :disabled="countdown > 0" @click="handleSendCode">
              {{ countdown > 0 ? `${countdown}s` : $t('login.sendCode') }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item :label="$t('login.newPassword')" prop="newPassword">
          <el-input
            v-model="resetForm.newPassword"
            type="password"
            show-password
            :placeholder="$t('login.placeholder.newPassword')"
          />
        </el-form-item>

        <el-form-item :label="$t('login.confirmPassword')" prop="confirmPassword">
          <el-input
            v-model="resetForm.confirmPassword"
            type="password"
            show-password
            :placeholder="$t('login.placeholder.confirmPassword')"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="w-full" :loading="loading" @click="handleSubmit">
            {{ $t('login.resetBtn') }}
          </el-button>
        </el-form-item>

        <div class="text-center">
          <router-link to="/login" class="text-sm text-blue-500">
            {{ $t('login.backToLogin') }}
          </router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { sendResetCode, resetPassword } from '@/api/auth'
import LangSelect from '@/components/LangSelect.vue'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()
const resetFormRef = ref<FormInstance>()
const loading = ref(false)
const countdown = ref(0)
let timer: any = null

const resetForm = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error(t('login.rules.confirmPasswordRequired')))
  } else if (value !== resetForm.newPassword) {
    callback(new Error(t('login.rules.passwordMismatch')))
  } else {
    callback()
  }
}

const rules = computed<FormRules>(() => ({
  email: [
    { required: true, message: t('login.rules.emailRequired'), trigger: 'blur' },
    { type: 'email', message: t('login.rules.emailInvalid'), trigger: 'blur' }
  ],
  code: [{ required: true, message: t('login.rules.codeRequired'), trigger: 'blur' }],
  newPassword: [{ required: true, message: t('login.rules.passwordRequired'), trigger: 'blur' }],
  confirmPassword: [{ validator: validatePass2, trigger: 'blur' }]
}))

const handleSendCode = async () => {
  if (!resetForm.email) {
    ElMessage.warning(t('login.rules.emailRequired'))
    return
  }
  // Validate email format simply
  if (!/^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/.test(resetForm.email)) {
    ElMessage.warning(t('login.rules.emailInvalid'))
    return
  }

  try {
    await sendResetCode(resetForm.email)
    ElMessage.success(t('login.codeSent'))
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    // Error handled by interceptor usually, but if not:
    console.error(error)
  }
}

const handleSubmit = async () => {
  if (!resetFormRef.value) return
  await resetFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await resetPassword({
          email: resetForm.email,
          code: resetForm.code,
          newPassword: resetForm.newPassword
        })
        ElMessage.success(t('login.resetSuccess'))
        router.push('/login')
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.reset-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.reset-card {
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
.items-center {
  align-items: center;
}
.mr-2 {
  margin-right: 0.5rem;
}
.flex-1 {
  flex: 1;
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
