<template>
  <view class="container">

    <view class="chat-header">
      <text class="chat-title">AI 智能助手</text>
    </view>

    <!-- 新增环境分析按钮 -->
    <button class="env-analyze-btn" @click="analyzeEnv">环境分析</button>
    <view v-if="analysisResult" class="analysis-result">
      {{ analysisResult }}
    </view>

    <!-- 聊天内容区域 -->
    <scroll-view class="chat-container" scroll-y scroll-with-animation :scroll-top="scrollTop">
      <view class="chat-list">
        <view v-for="(msg, index) in chatMessages" :key="index" 
              :class="['chat-item', msg.type === 'user' ? 'chat-right' : 'chat-left']">
          <view class="chat-avatar">
            <text>{{ msg.type === 'user' ? '👤' : '🤖' }}</text>
          </view>
          <view class="chat-content">
            <text>{{ msg.content }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="chat-input-container">
      <input class="chat-input" 
              type="text" 
              v-model="inputMessage" 
              placeholder="请输入您的问题"
              :disabled="chatLoading"
              @confirm="sendMessage" />
      <button class="voice-btn" 
              @click="goToVoiceChat">
        <text class="voice-icon">🎤</text>
      </button>
      <button class="send-btn" 
              @click="sendMessage" 
              :disabled="!inputMessage.trim() || chatLoading">
        {{ chatLoading ? '请稍候...' : '发送' }}
      </button>
    </view>

    <!-- 底部导航栏 -->
    <view class="tab-bar">
      <view class="tab-item" @click="switchTab('monitor')">
        <text class="tab-icon">📺</text>
        <text class="tab-text">监控</text>
      </view>
      <view class="tab-item active">
        <text class="tab-icon">🤖</text>
        <text class="tab-text">AI</text>
      </view>
      <view class="tab-item" @click="switchTab('person')">
        <text class="tab-icon">👤</text>
        <text class="tab-text">我的</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { BASE_URL } from '@/config/config.js'

// 聊天模式数据
const chatMessages = ref([
  { type: 'ai', content: '你好！我是智慧教室AI助手，我可以帮你：\n1. 分析环境数据\n2. 控制教室设备\n3. 回答教室相关问题\n你可以输入文字或点击麦克风按钮进入语音对话模式。' }
])
const inputMessage = ref('')
const chatLoading = ref(false)
const scrollTop = ref(0)

// 新增环境分析相关
const analysisResult = ref('')

// 发送聊天消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || chatLoading.value) return
  
  const userMessage = inputMessage.value.trim()
  chatMessages.value.push({ type: 'user', content: userMessage })
  inputMessage.value = ''
  chatLoading.value = true
  
  // 自动滚动到底部
  scrollToBottom()
  
  try {
    const response = await uni.request({
      url: `${BASE_URL}/api/askDeepSeek?question=${encodeURIComponent(userMessage)}`,
      method: 'GET'
    })

    if (response.statusCode === 200) {
      let aiReply = ''
      if (typeof response.data === 'string') {
        aiReply = response.data
      } else if (response.data && response.data.answer) {
        aiReply = response.data.answer
      }
      if (aiReply) {
        chatMessages.value.push({ type: 'ai', content: aiReply })
      } else {
        chatMessages.value.push({ type: 'ai', content: 'AI暂时无法回答，请稍后再试。' })
      }
    } else {
      chatMessages.value.push({ type: 'ai', content: 'AI暂时无法回答，请稍后再试。' })
    }
  } catch (error) {
    console.error('AI回复失败:', error)
    chatMessages.value.push({ type: 'ai', content: '抱歉，我现在无法回答，请稍后再试。' })
  } finally {
    chatLoading.value = false
    scrollToBottom()
  }
}

// 滚动到底部
const scrollToBottom = () => {
  setTimeout(() => {
    const query = uni.createSelectorQuery()
    query.select('.chat-container').boundingClientRect()
    query.select('.chat-list').boundingClientRect()
    query.exec((res) => {
      if (res[0] && res[1]) {
        scrollTop.value = res[1].height - res[0].height
      }
    })
  }, 100)
}

// 跳转到语音对话页面
const goToVoiceChat = () => {
  uni.navigateTo({
    url: '/pages/index/ai-chat'
  })
}

// 切换底部导航
const switchTab = (tab) => {
  switch (tab) {
    case 'monitor':
      uni.reLaunch({ url: '/pages/index/home' })
      break
    case 'person':
      uni.reLaunch({ url: '/pages/index/person' })
      break
  }
}

function analyzeEnv() {
  const envHistory = uni.getStorageSync('envHistory')
  if (!envHistory || !envHistory.temp || envHistory.temp.length === 0) {
    analysisResult.value = '暂无环境历史数据，无法分析。'
    return
  }
  // 简单分析逻辑
  const temps = envHistory.temp
  const hums = envHistory.humidity
  const tempMax = Math.max(...temps)
  const tempMin = Math.min(...temps)
  const humMax = Math.max(...hums)
  const humMin = Math.min(...hums)
  let result = `温度区间：${tempMin}~${tempMax}°C，湿度区间：${humMin}~${humMax}%。\n`
  if (tempMax > 30) result += '温度偏高，请注意降温。'
  else if (tempMin < 15) result += '温度偏低，请注意保暖。'
  else result += '温度适宜。'
  if (humMax > 80) result += '湿度偏高，注意防潮。'
  else if (humMin < 30) result += '湿度偏低，注意加湿。'
  else result += '湿度适宜。'
  analysisResult.value = result
}
</script>

<style scoped>
.container {
  padding: 30rpx;
  background-color: #f5f6fa;
  min-height: 100vh;
  padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
}

.chat-header {
  background: linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%);
  padding: 40rpx 30rpx;
  border-radius: 20rpx;
  color: #fff;
  margin-bottom: 30rpx;
}

.chat-title {
  font-size: 40rpx;
  font-weight: bold;
}

.chat-container {
  width: fit-content;
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 120rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  height: calc(100vh - 400rpx);
}

.chat-list {
  padding-bottom: 30rpx;
}

.chat-item {
  display: flex;
  margin-bottom: 30rpx;
  align-items: flex-start;
}

.chat-left {
  flex-direction: row;
}

.chat-right {
  flex-direction: row-reverse;
}

.chat-avatar {
  width: 80rpx;
  height: 80rpx;
  background: #f0f7ff;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  flex-shrink: 0;
}

.chat-content {
  max-width: 70%;
  margin: 0 20rpx;
  padding: 20rpx;
  border-radius: 10rpx;
  font-size: 28rpx;
  line-height: 1.5;
}

.chat-left .chat-content {
  background: #f0f7ff;
  color: #333;
}

.chat-right .chat-content {
  background: #1a73e8;
  color: #fff;
}

.chat-input-container {
  position: fixed;
  bottom: calc(100rpx + env(safe-area-inset-bottom));
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: #fff;
  display: flex;
  align-items: center;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.chat-input {
  flex: 1;
  height: 80rpx;
  background: #f5f6fa;
  border-radius: 40rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
  margin-right: 20rpx;
}

.voice-btn {
  width: 80rpx;
  height: 80rpx;
  background: #f5f6fa;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  padding: 0;
  transition: all 0.3s ease;
}

.voice-btn.listening {
  background: #1a73e8;
}

.voice-btn.listening .voice-icon {
  color: #fff;
}

.voice-icon {
  font-size: 40rpx;
  color: #666;
}

.send-btn {
  width: 160rpx;
  height: 80rpx;
  background: #1a73e8;
  color: #fff;
  border-radius: 40rpx;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.send-btn[disabled] {
  background: #a0c4f1;
}

/* 底部导航栏样式 */
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background: #ffffff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  padding-bottom: env(safe-area-inset-bottom);
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
}

.tab-item.active {
  color: #1a73e8;
}

.tab-icon {
  font-size: 40rpx;
  margin-bottom: 4rpx;
}

.tab-text {
  font-size: 24rpx;
}

.env-analyze-btn {
  width: 90%;
  margin: 20rpx 5%;
  background: #1a73e8;
  color: #fff;
  border-radius: 16rpx;
  font-size: 32rpx;
  padding: 20rpx 0;
}
.analysis-result {
  margin: 20rpx 5%;
  background: #f5f6fa;
  padding: 24rpx;
  border-radius: 12rpx;
  color: #333;
  font-size: 28rpx;
  white-space: pre-line;
}
</style>
