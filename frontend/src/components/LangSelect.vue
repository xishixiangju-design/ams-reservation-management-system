<template>
  <el-dropdown trigger="click" @command="handleSetLanguage">
    <span class="el-dropdown-link">
      {{ language === 'zh-CN' ? '中文' : '日本語' }}
      <el-icon class="el-icon--right"><arrow-down /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item :disabled="language === 'zh-CN'" command="zh-CN"> 中文 </el-dropdown-item>
        <el-dropdown-item :disabled="language === 'ja-JP'" command="ja-JP">
          日本語
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowDown } from '@element-plus/icons-vue'

const { locale } = useI18n()

const language = computed(() => locale.value)

const handleSetLanguage = (lang: string) => {
  locale.value = lang
  localStorage.setItem('language', lang)
  // Reload page to ensure all components (especially those not reactive to locale) update?
  // With Vue I18n and reactive Element Plus config, reload shouldn't be strictly necessary for most things.
  // But for some deep integrations it might be safer. For now, let's try without reload.
}
</script>

<style scoped>
.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  margin-right: 20px;
}
</style>
