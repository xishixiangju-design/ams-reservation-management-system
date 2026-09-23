<template>
  <div class="member-center">
    <div class="level-card" :class="memberInfo.currentLevel?.toLowerCase()">
      <div class="card-header">
        <div class="level-icon">
          <el-icon v-if="!memberInfo.iconUrl" size="40"><Trophy /></el-icon>
          <img v-else :src="memberInfo.iconUrl" alt="level" />
        </div>
        <div class="level-info">
          <h2>{{ memberInfo.levelName }}</h2>
          <p>
            {{ $t('member.currentDiscount') }}: {{ (memberInfo.discountRate * 10).toFixed(1) }}折
          </p>
        </div>
      </div>

      <div v-if="!memberInfo.isMax" class="progress-section">
        <div class="progress-text">
          <span>{{ $t('member.consumption') }}: ¥{{ memberInfo.totalConsumption }}</span>
          <span
            >{{ $t('member.nextLevel', { level: memberInfo.nextLevelName }) }}:
            {{ $t('member.need') }} ¥{{ memberInfo.needAmount }}</span
          >
        </div>
        <el-progress
          :percentage="memberInfo.progress || 0"
          :stroke-width="10"
          status="success"
          :format="formatProgress"
        />
      </div>
      <div v-else class="max-level">
        <p>{{ $t('member.maxLevel', { level: memberInfo.levelName }) }}</p>
      </div>
    </div>

    <div class="rules-section">
      <h3>
        <el-icon><Star /></el-icon> {{ $t('member.rights') }}
      </h3>
      <el-table :data="rules" style="width: 100%" stripe>
        <el-table-column prop="name" :label="$t('member.columns.name')" width="120" />
        <el-table-column :label="$t('member.columns.threshold')">
          <template #default="scope"> ¥{{ scope.row.minConsumption }} </template>
        </el-table-column>
        <el-table-column :label="$t('member.columns.discount')">
          <template #default="scope"> {{ (scope.row.discountRate * 10).toFixed(1) }}折 </template>
        </el-table-column>
        <el-table-column prop="rightsDesc" :label="$t('member.columns.desc')" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMemberInfo, getLevelRules, type MemberInfo, type MemberLevelRule } from '@/api/member'
import { Trophy, Star } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const memberInfo = ref<MemberInfo>({
  totalConsumption: 0,
  currentLevel: 'NORMAL',
  discountRate: 1,
  levelName: t('customer.level.normal')
})

const rules = ref<MemberLevelRule[]>([])

const formatProgress = (percentage: number) => {
  return `${percentage.toFixed(0)}%`
}

onMounted(async () => {
  try {
    const infoRes = await getMemberInfo()
    memberInfo.value = infoRes.data

    const rulesRes = await getLevelRules()
    rules.value = rulesRes.data
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.member-center {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.level-card {
  background: linear-gradient(135deg, #a8c0ff 0%, #3f2b96 100%); /* Default/Normal */
  color: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.level-card.bronze {
  background: linear-gradient(135deg, #cd9cf2 0%, #f6f3ff 100%); /* Adjust colors */
  background: linear-gradient(to right, #b06ab3, #4568dc);
}
.level-card.silver {
  background: linear-gradient(to right, #e0eafc, #cfdef3);
  color: #333;
}
.level-card.gold {
  background: linear-gradient(to right, #cac531, #f3f9a7);
  color: #5d4e00;
}
.level-card.diamond {
  background: linear-gradient(to right, #24c6dc, #514a9d);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.level-icon {
  margin-right: 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.level-info h2 {
  margin: 0;
  font-size: 24px;
}

.level-info p {
  margin: 4px 0 0;
  opacity: 0.9;
}

.progress-section .progress-text {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.rules-section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: #303133;
}
</style>
