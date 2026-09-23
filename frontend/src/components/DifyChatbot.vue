<template>
  <!-- Dify Chatbot Bubble (injected via script) -->
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'

const SCRIPT_ID = 'Dh6xHTXjXbUG7EVs'
const STYLE_ID = 'dify-chatbot-style'

onMounted(() => {
  // 避免重复注入
  if (document.getElementById(SCRIPT_ID)) return

  // 注入配置
  ;(window as any).difyChatbotConfig = {
    token: 'Dh6xHTXjXbUG7EVs',
    inputs: {},
    systemVariables: {},
    userVariables: {}
  }

  // 注入加载脚本
  const script = document.createElement('script')
  script.src = 'https://udify.app/embed.min.js'
  script.id = SCRIPT_ID
  script.defer = true
  document.body.appendChild(script)

  // 注入样式
  if (!document.getElementById(STYLE_ID)) {
    const style = document.createElement('style')
    style.id = STYLE_ID
    style.textContent = `
      #dify-chatbot-bubble-button {
        background-color: #1C64F2 !important;
      }
      #dify-chatbot-bubble-window {
        width: 24rem !important;
        height: 40rem !important;
      }
    `
    document.head.appendChild(style)
  }
})

onUnmounted(() => {
  // 移除脚本
  const script = document.getElementById(SCRIPT_ID)
  if (script) script.remove()

  // 移除样式
  const style = document.getElementById(STYLE_ID)
  if (style) style.remove()

  // 移除聊天气泡 DOM 元素（由 embed.min.js 动态插入）
  const bubble = document.getElementById('dify-chatbot-bubble-button')
  if (bubble) bubble.parentElement?.remove()
  const chatWindow = document.getElementById('dify-chatbot-bubble-window')
  if (chatWindow) chatWindow.parentElement?.remove()
})
</script>
