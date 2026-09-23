<template>
  <div class="register-container">
    <div class="lang-switch">
      <LangSelect />
    </div>
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <span>{{ $t('register.title') }}</span>
        </div>
      </template>
      <el-form ref="registerFormRef" :model="registerForm" :rules="rules" label-position="top">
        <el-form-item :label="$t('register.username')" prop="username">
          <el-input
            v-model="registerForm.username"
            :placeholder="$t('register.placeholder.username')"
          />
        </el-form-item>
        <el-form-item :label="$t('register.nickname')" prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            :placeholder="$t('register.placeholder.nickname')"
          />
        </el-form-item>
        <el-form-item :label="$t('register.email')" prop="email">
          <el-input v-model="registerForm.email" :placeholder="$t('register.placeholder.email')" />
        </el-form-item>
        <el-form-item :label="$t('register.password')" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            show-password
            :placeholder="$t('register.placeholder.password')"
          />
        </el-form-item>
        <el-form-item :label="$t('register.confirmPassword')" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            show-password
            :placeholder="$t('register.placeholder.confirmPassword')"
          />
        </el-form-item>

        <el-form-item :label="$t('register.captcha')" prop="userInputCaptcha">
          <div class="flex items-center w-full">
            <el-input
              v-model="registerForm.userInputCaptcha"
              :placeholder="$t('register.placeholder.captcha')"
              class="flex-1 mr-2"
            />
            <img
              v-if="captchaUrl"
              :src="captchaUrl"
              class="cursor-pointer h-8"
              alt="captcha"
              title="Refresh"
              style="height: 32px; border: 1px solid #dcdfe6; border-radius: 4px"
              @click="refreshCaptcha"
            />
          </div>
        </el-form-item>

        <el-form-item prop="agree">
          <el-checkbox v-model="registerForm.agree">
            {{ $t('register.agree') }}
            <el-link type="primary">{{ $t('register.terms') }}</el-link> &
            <el-link type="primary">{{ $t('register.privacy') }}</el-link>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="w-full" :loading="loading" @click="handleRegister">{{
            $t('register.submit')
          }}</el-button>
        </el-form-item>
        <div class="text-center">
          <router-link to="/login" class="text-sm text-blue-500">{{
            $t('register.hasAccount')
          }}</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getCaptcha } from '@/api/auth'
import { useI18n } from 'vue-i18n'
import LangSelect from '@/components/LangSelect.vue'

const { t } = useI18n()
const router = useRouter()
const registerFormRef = ref<FormInstance>()

const registerForm = reactive({
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
  userInputCaptcha: '',
  captchaUuid: '',
  agree: false
})

const captchaUrl = ref('')

const refreshCaptcha = async () => {
  try {
    const res: any = await getCaptcha()
    if (res.code === 200) {
      captchaUrl.value = res.data.image
      registerForm.captchaUuid = res.data.uuid
    }
  } catch (error) {
    console.error(error)
  }
}

const loading = ref(false)

const validatePass2 = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error(t('register.placeholder.confirmPassword')))
  } else if (value !== registerForm.password) {
    callback(new Error(t('register.rules.confirmPassword')))
  } else {
    callback()
  }
}

const rules = computed<FormRules>(() => ({
  username: [{ required: true, message: t('register.rules.username'), trigger: 'blur' }],
  nickname: [{ required: true, message: t('register.rules.nickname'), trigger: 'blur' }],
  email: [
    { required: true, message: t('register.rules.email'), trigger: 'blur' },
    { type: 'email', message: t('register.rules.email'), trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: t('register.rules.password'), trigger: 'blur' },
    { min: 6, message: t('register.rules.password'), trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validatePass2, trigger: 'blur' }],
  userInputCaptcha: [{ required: true, message: t('register.rules.captcha'), trigger: 'blur' }],
  agree: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error(t('register.rules.agree')))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}))

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register({
          username: registerForm.username,
          password: registerForm.password,
          nickname: registerForm.nickname,
          email: registerForm.email,
          captchaUuid: registerForm.captchaUuid,
          userInputCaptcha: registerForm.userInputCaptcha
        })
        ElMessage.success(t('register.success'))
        router.push('/login')
      } catch (error) {
        refreshCaptcha()
        registerForm.userInputCaptcha = ''
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
.lang-switch {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10;
}
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 20px;
  position: relative;
}
.register-card {
  width: 100%;
  max-width: 400px;
}
.w-full {
  width: 100%;
}
.flex {
  display: flex;
}
.gap-2 {
  gap: 0.5rem;
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
