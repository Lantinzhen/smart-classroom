<template>
  <view class="container">
    <!-- 加载状态遮罩 -->
    <view class="loading-mask" v-if="isLoading">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 连接状态提示 -->
    <view class="connection-status" :class="{ 'connected': isConnected, 'disconnected': !isConnected }">
      <text class="status-dot"></text>
      <text class="status-text">{{ isConnected ? '已连接' : '未连接' }}</text>
    </view>

    <!-- 顶部状态卡片 -->
    <view class="card status-card">
      <view class="card-header">
        <text class="card-title">教室状态</text>
        <text class="more">●</text>
      </view>
      <view class="card-content">
        <view class="status-grid">
          <view class="status-item">
            <text class="status-label">当前人数</text>
            <text class="status-value">{{ peopleCount }}人</text>
          </view>
          <view class="status-item">
            <text class="status-label">空气质量</text>
            <text class="status-value">{{ airQuality }}</text>
          </view>
          <view class="status-item">
            <text class="status-label">教室状态</text>
            <text class="status-value">{{ classroomStatus }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 控制面板 -->
    <view class="control-grid">
      <!-- 门锁控制 -->
      <view class="control-item" :class="{ active: doorLocked }" @click="toggleDoor">
        <view class="control-icon">🚪</view>
        <text class="control-label">门锁</text>
        <text class="control-status">{{ doorLocked ? '已锁' : '未锁' }}</text>
      </view>

      <!-- 灯光控制 -->
      <view class="control-item" :class="{ active: lightsOn }" @click="toggleLights">
        <view class="control-icon">💡</view>
        <text class="control-label">灯光</text>
        <text class="control-status">{{ lightsOn ? '开启' : '关闭' }}</text>
      </view>

      <!-- 风扇控制 -->
      <view class="control-item" :class="{ active: fanOn }" @click="toggleFan">
        <view class="control-icon">💨</view>
        <text class="control-label">风扇</text>
        <text class="control-status">{{ fanOn ? '开启' : '关闭' }}</text>
      </view>

      <!-- 温度显示 -->
      <view class="control-item" @click="showTempChart = true">
        <view class="control-icon">🌡️</view>
        <text class="control-label">温度</text>
        <text class="control-status">{{ temperature }}°C</text>
        <text class="control-tip">点击查看趋势</text>
      </view>

      <!-- 湿度显示 -->
      <view class="control-item" @click="showHumidityChart = true">
        <view class="control-icon">💧</view>
        <text class="control-label">湿度</text>
        <text class="control-status">{{ humidity }}%</text>
        <text class="control-tip">点击查看趋势</text>
      </view>

      <!-- 城市温湿度查询 -->
      <view class="control-item" @click="showCity = true">
        <view class="control-icon">🌡️</view>
        <text class="control-label">查询城市温湿度</text>
        <text class="control-tip">点击查看趋势</text>
      </view>
    </view>

    <!-- 温度趋势图弹窗 -->
    <view class="modal" v-if="showTempChart" @click="showTempChart = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">温度变化趋势</text>
          <text class="modal-close" @click="showTempChart = false">×</text>
        </view>
        <view class="chart-container">
          <canvas canvas-id="tempChart" id="tempChart" class="chart"></canvas>
        </view>
      </view>
    </view>

    <!-- 湿度趋势图弹窗 -->
    <view class="modal" v-if="showHumidityChart" @click="showHumidityChart = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">湿度变化趋势</text>
          <text class="modal-close" @click="showHumidityChart = false">×</text>
        </view>
        <view class="chart-container">
          <canvas canvas-id="humidityChart" id="humidityChart" class="chart"></canvas>
        </view>
      </view>
    </view>

    <!-- 城市温湿度查询弹窗 -->
    <view class="modal" v-if="showCity" @click="showCity = false">
      <view class="modal-content city-modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">城市温湿度查询</text>
          <text class="modal-close" @click="showCity = false">×</text>
        </view>
        <view class="city-input-row">
          <input
            v-model="cityInput"
            placeholder="请输入城市名（如beijing/shanghai）"
            class="city-input"
            @keyup.enter="fetchCityWeather"
          />
          <button class="city-btn" @click="fetchCityWeather" :disabled="cityLoading">
            {{ cityLoading ? '查询中...' : '查询' }}
          </button>
        </view>
        <view class="city-result" v-if="cityWeather">
          <view class="city-row"><text class="city-label">温度：</text><text class="city-value">{{ cityWeather.temperature }}°C</text></view>
        </view>
        <view v-else-if="cityError" class="city-error">{{ cityError }}</view>
      </view>
    </view>

    <!-- 火焰警报弹窗 -->
    <view class="modal" v-if="showFireAlert" @click="showFireAlert = false">
      <view class="modal-content alert-modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">火焰警报</text>
          <text class="modal-close" @click="showFireAlert = false">×</text>
        </view>
        <view class="alert-message">检测到火焰，请及时处理！</view>
      </view>
    </view>

    <!-- 下雨警报弹窗 -->
    <view class="modal" v-if="showRainAlert" @click="showRainAlert = false">
      <view class="modal-content alert-modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">下雨警报</text>
          <text class="modal-close" @click="showRainAlert = false">×</text>
        </view>
        <view class="alert-message">检测到下雨，请注意防护！</view>
      </view>
    </view>

    <!-- 底部导航栏 -->
    <view class="tab-bar">
      <view class="tab-item" :class="{ active: currentTab === 'monitor' }" @click="switchTab('monitor')">
        <text class="tab-icon">📺</text>
        <text class="tab-text">监控</text>
      </view>
      <view class="tab-item" :class="{ active: currentTab === 'ai' }" @click="switchTab('ai')">
        <text class="tab-icon">🤖</text>
        <text class="tab-text">AI</text>
      </view>
      <view class="tab-item" :class="{ active: currentTab === 'person' }" @click="switchTab('person')">
        <text class="tab-icon">👤</text>
        <text class="tab-text">我的</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import uCharts from '@qiun/ucharts'
import { BASE_URL, WS } from '@/config/config.js'
console.log('BASE_URL: ' + BASE_URL)
console.log('WS: ' + WS)
// 状态变量
const doorLocked = ref(true)
const lightsOn = ref(false)
const fanOn = ref(false)
const peopleCount = ref(0)
const temperature = ref(25)
const humidity = ref(60)
const currentTab = ref('monitor')
const showTempChart = ref(false)
const showHumidityChart = ref(false)
const isLoading = ref(false)
const isConnected = ref(false)
const reconnectAttempts = ref(0)
const MAX_RECONNECT_ATTEMPTS = 5
const showCity = ref(false)
const cityInput = ref('')
const cityWeather = ref(null)
const cityLoading = ref(false)
const cityError = ref('')
const showFireAlert = ref(false)
const showRainAlert = ref(false)

// 历史数据
const tempHistory = ref([])
const tempTimeHistory = ref([])
const humidityHistory = ref([])
const humidityTimeHistory = ref([])

// 图表实例
let tempChart = null
let humidityChart = null

// 初始化温度图表
const initTempChart = () => {
  const ctx = uni.createCanvasContext('tempChart', this)
  const emptyCategories = Array(tempHistory.value.length).fill('');
  tempChart = new uCharts({
    context: ctx,
    type: 'line',
    categories: emptyCategories,
    series: [{
      name: '温度',
      data: tempHistory.value
    }],
    width: 300,
    height: 400,
    padding: [15, 15, 0, 15],
    xAxis: {
      disableGrid: true,
      type: 'category',
      boundaryGap: false,
      itemCount: 6,
      disableLabel: true
    },
    yAxis: {
      gridType: 'dash',
      dashLength: 2,
      data: [{
        title: '温度(°C)',
        min: 0,
        max: 40
      }]
    },
    extra: {
      line: {
        type: 'curve',
        width: 2,
        connectNulls: true
      }
    }
  })
  tempChart.updateData()
}

// 初始化湿度图表
const initHumidityChart = () => {
  const ctx = uni.createCanvasContext('humidityChart', this)
  const emptyCategories = Array(humidityHistory.value.length).fill('');
  humidityChart = new uCharts({
    context: ctx,
    type: 'line',
    categories: emptyCategories,
    series: [{
      name: '湿度',
      data: humidityHistory.value
    }],
    width: 300,
    height: 400,
    padding: [15, 15, 0, 15],
    xAxis: {
      disableGrid: true,
      type: 'category',
      boundaryGap: false,
      itemCount: 6,
      disableLabel: true
    },
    yAxis: {
      gridType: 'dash',
      dashLength: 2,
      data: [{
        title: '湿度(%)',
        min: 0,
        max: 100
      }]
    },
    extra: {
      line: {
        type: 'curve',
        width: 2,
        connectNulls: true
      }
    }
  })
  humidityChart.updateData()
}

// 更新图表数据
const updateCharts = () => {
  const emptyTempCategories = Array(tempHistory.value.length).fill('');
  const emptyHumidityCategories = Array(humidityHistory.value.length).fill('');
  if (tempChart) {
    tempChart.updateData({
      categories: emptyTempCategories,
      series: [{
        name: '温度',
        data: tempHistory.value
      }]
    })
  }
  if (humidityChart) {
    humidityChart.updateData({
      categories: emptyHumidityCategories,
      series: [{
        name: '湿度',
        data: humidityHistory.value
      }]
    })
  }
}

// 监听数据变化，更新图表
watch([tempHistory, humidityHistory, tempTimeHistory, humidityTimeHistory], () => {
  updateCharts()
})

// 监听弹窗显示状态，初始化图表
watch(showTempChart, (newVal) => {
  if (newVal) {
    nextTick(() => {
      initTempChart()
    })
  }
})

watch(showHumidityChart, (newVal) => {
  if (newVal) {
    nextTick(() => {
      initHumidityChart()
    })
  }
})

// 时间格式化
function formatNowTime() {
  const d = new Date();
  return `${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`;
}

// 轮询定时器
let pollingTimer = null;

// 只轮询温湿度
function startPolling() {
  pollingTimer = setInterval(() => {
    fetchTemperatureAndHumidity();
  }, 3000); // 每3秒
}
function stopPolling() {
  if (pollingTimer) clearInterval(pollingTimer);
}

// 只获取温湿度接口，并记录本地时间
const fetchTemperatureAndHumidity = async () => {
  try {
    const [tempRes, humidityRes] = await Promise.all([
      uni.request({ url: `${BASE_URL}/api/ohos/temperatures` }),
      uni.request({ url: `${BASE_URL}/api/ohos/humidities` })
    ]);
    // 只取最新一条
    if (tempRes.data && tempRes.data.length) {
      const temp = Number(tempRes.data[0]);
      tempHistory.value.push(temp);
      tempTimeHistory.value.push(formatNowTime());
      if (tempHistory.value.length > 10) {
        tempHistory.value.shift();
        tempTimeHistory.value.shift();
      }
      temperature.value = temp;
    }
    if (humidityRes.data && humidityRes.data.length) {
      const hum = Number(humidityRes.data[0]);
      humidityHistory.value.push(hum);
      humidityTimeHistory.value.push(formatNowTime());
      if (humidityHistory.value.length > 10) {
        humidityHistory.value.shift();
        humidityTimeHistory.value.shift();
      }
      humidity.value = hum;
    }
    // 每次采集后实时更新图表
    updateCharts();
    // 存储历史数据到本地，供AI页面分析
    uni.setStorageSync('envHistory', {
      temp: tempHistory.value,
      humidity: humidityHistory.value,
      tempTime: tempTimeHistory.value,
      humidityTime: humidityTimeHistory.value
    });
  } catch (error) {
    console.error('温湿度轮询失败:', error)
  }
}

// 计算属性
const airQuality = computed(() => {
  if (temperature.value > 30 || humidity.value > 80) return '较差'
  if (temperature.value > 28 || humidity.value > 70) return '一般'
  return '良好'
})

const classroomStatus = computed(() => {
  if (peopleCount.value === 0) return '空闲'
  if (peopleCount.value < 30) return '使用中'
  return '满员'
})

// WebSocket连接
let ws = null

let heartbeatTimer = null;
function startHeartbeat() {
  heartbeatTimer = setInterval(() => {
    if (ws && isConnected.value) {
      ws.send({ data: JSON.stringify({ type: 'ping' }) });
    }
  }, 30000); // 每30秒
}
function stopHeartbeat() {
  if (heartbeatTimer) clearInterval(heartbeatTimer);
}

// 从本地存储加载状态
const loadState = () => {
  try {
    const savedState = uni.getStorageSync('classroomState')
    if (savedState) {
      const state = JSON.parse(savedState)
      doorLocked.value = state.doorLocked
      lightsOn.value = state.lightsOn
      fanOn.value = state.fanOn
      peopleCount.value = state.peopleCount
      temperature.value = state.temperature
      humidity.value = state.humidity
    }
  } catch (error) {
    console.error('加载状态失败:', error)
  }
}

// 保存状态到本地存储
const saveState = () => {
  try {
    const state = {
      doorLocked: doorLocked.value,
      lightsOn: lightsOn.value,
      fanOn: fanOn.value,
      peopleCount: peopleCount.value,
      temperature: temperature.value,
      humidity: humidity.value
    }
    uni.setStorageSync('classroomState', JSON.stringify(state))
  } catch (error) {
    console.error('保存状态失败:', error)
  }
}

// 初始化WebSocket连接
const initWebSocket = () => {
  isLoading.value = true
  ws = uni.connectSocket({
    url: `ws://${WS}/ws`,
    success: () => {
      console.log('WebSocket连接成功')
      isConnected.value = true
      reconnectAttempts.value = 0
      isLoading.value = false
      startHeartbeat()
    },
    fail: () => {
      console.error('WebSocket连接失败')
      handleConnectionError()
    }
  })
  
  ws.onOpen(() => {
    console.log('WebSocket连接已建立')
    isConnected.value = true
    reconnectAttempts.value = 0
    isLoading.value = false
    startHeartbeat()
  })
  
  ws.onClose(() => {
    console.log('WebSocket连接已关闭')
    isConnected.value = false
    handleConnectionError()
    stopHeartbeat()
  })
  
  ws.onError(() => {
    console.error('WebSocket连接错误')
    handleConnectionError()
    stopHeartbeat()
  })
  
  ws.onMessage((res) => {
    try {
      // 判断是否为JSON字符串
      if (typeof res.data === 'string' && res.data.trim().startsWith('{')) {
        const data = JSON.parse(res.data)
        console.log(data);
        handleWebSocketMessage(data)
        saveState()
      } else {
        // 非JSON消息（如"连接成功"），可忽略或做日志
        console.log('收到非JSON消息:', res.data)
      }
    } catch (error) {
      console.error('处理WebSocket消息失败:', error)
    }
  })
}

// 处理连接错误
const handleConnectionError = () => {
  isLoading.value = false
  if (reconnectAttempts.value < MAX_RECONNECT_ATTEMPTS) {
    reconnectAttempts.value++
    setTimeout(() => {
      console.log(`尝试重新连接 (${reconnectAttempts.value}/${MAX_RECONNECT_ATTEMPTS})`)
      initWebSocket()
    }, 3000)
  } else {
    uni.showToast({
      title: '连接失败，请检查网络',
      icon: 'none',
      duration: 2000
    })
  }
}

// 处理WebSocket消息
const handleWebSocketMessage = (data) => {
  switch (data.signal) {
    case 0: // 人离开
      peopleCount.value = Math.max(0, peopleCount.value - 1)
      break
    case 1: // 人到来
      peopleCount.value += 1
      break
    case 4: // 温度数据
      temperature.value = Number(data.value)
      if (tempHistory.value.length >= 24) {
        tempHistory.value.shift()
      }
      tempHistory.value.push(Number(data.value))
      break
    case 9: // 湿度数据
      humidity.value = Number(data.value)
      if (humidityHistory.value.length >= 24) {
        humidityHistory.value.shift()
      }
      humidityHistory.value.push(Number(data.value))
      break
    case 50: // 火焰警报
      showFireAlert.value = true
      showRainAlert.value = false
      break
    case 51: // 下雨警报
      showRainAlert.value = true
      showFireAlert.value = false
      break
    default:
      // 其他信号关闭所有警报弹窗
      showFireAlert.value = false
      showRainAlert.value = false
      break
  }
}

// 切换门锁状态
const toggleDoor = () => {
  const signal = doorLocked.value ? 6 : 7 // 6开门，7关门
  sendMQTTMessage(signal, 0)
  doorLocked.value = !doorLocked.value
}

// 切换灯光状态
const toggleLights = () => {
  const signal = lightsOn.value ? 3 : 2 // 2开灯，3关灯
  sendMQTTMessage(signal, 0)
  lightsOn.value = !lightsOn.value
}

// 切换风扇状态
const toggleFan = () => {
  const signal = fanOn.value ? 11 : 10 // 10开风扇，11关风扇
  sendMQTTMessage(signal, 0)
  fanOn.value = !fanOn.value
}

// 发送MQTT消息
const sendMQTTMessage = async (signal, value) => {
  if (!isConnected.value) {
    uni.showToast({
      title: '设备未连接',
      icon: 'none'
    })
    return
  }

  try {
    isLoading.value = true
    const response = await uni.request({
      url: `${BASE_URL}/send`,
      method: 'POST',
      data: {
        signal,
        value
      }
    })
    console.log('MQTT消息发送成功:', response)
    saveState() // 发送消息后保存状态
  } catch (error) {
    console.error('MQTT消息发送失败:', error)
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    })
  } finally {
    isLoading.value = false
  }
}

// 获取初始数据
const fetchInitialData = async () => {
  try {
    const [tempRes, humidityRes] = await Promise.all([
      uni.request({ url: `${BASE_URL}/api/ohos/temperatures` }),
      uni.request({ url: `${BASE_URL}/api/ohos/humidities` })
    ])

    if (tempRes.data && Array.isArray(tempRes.data)) {
      tempHistory.value = tempRes.data.slice(-24).map(Number)
      temperature.value = Number(tempRes.data[0])
    }
    if (humidityRes.data && Array.isArray(humidityRes.data)) {
      humidityHistory.value = humidityRes.data.slice(-24).map(Number)
      humidity.value = Number(humidityRes.data[0])
    }
  } catch (error) {
    console.error('获取初始数据失败:', error)
    uni.showToast({
      title: '获取数据失败',
      icon: 'none'
    })
  }
}

// 查询城市天气
const fetchCityWeather = async () => {
  if (!cityInput.value) {
    cityError.value = '请输入城市名';
    cityWeather.value = null;
    return;
  }
  cityLoading.value = true;
  cityError.value = '';
  cityWeather.value = null;
  try {
    const res = await uni.request({
      url: `https://api.seniverse.com/v3/weather/now.json`,
      method: 'GET',
      data: {
        key: 'STPIDvY0xCbOTCE5p',
        location: cityInput.value,
        language: 'zh-Hans',
        unit: 'c'
      }
    })
    if (
      res.data &&
      res.data.results &&
      res.data.results[0] &&
      res.data.results[0].now
    ) {
      cityWeather.value = {
        temperature: res.data.results[0].now.temperature,
      }
    } else {
      cityError.value = '未查询到该城市天气';
    }
  } catch (e) {
    cityError.value = '查询失败，请检查网络或城市名';
  } finally {
    cityLoading.value = false;
  }
}

// 监听窗口大小变化
const handleResize = () => {
  if (tempChart) {
    tempChart.updateData({
      categories: tempTimeHistory.value,
      series: [{
        name: '温度',
        data: tempHistory.value
      }]
    })
  }
  if (humidityChart) {
    humidityChart.updateData({
      categories: humidityTimeHistory.value,
      series: [{
        name: '湿度',
        data: humidityHistory.value
      }]
    })
  }
}

onMounted(() => {
  loadState()
  initWebSocket()
  startPolling()
  fetchInitialData() // 获取初始数据
  
  // 使用 uni-app 的页面生命周期
  uni.onWindowResize(handleResize)
  
  uni.onAppShow(() => {
    if (!isConnected.value) {
      initWebSocket()
    }
    startPolling()
  })
})

onUnmounted(() => {
  stopPolling()
  if (ws) {
    ws.close()
  }
  // 移除窗口大小变化监听，传入相同的回调函数
  uni.offWindowResize(handleResize)
  if (tempChart) {
    tempChart = null
  }
  if (humidityChart) {
    humidityChart = null
  }
})

// 切换底部导航
const switchTab = (tab) => {
  if (tab === currentTab.value) return
  
  switch (tab) {
    case 'monitor':
      uni.reLaunch({ url: '/pages/index/home' })
      break
    case 'ai':
      uni.reLaunch({ url: '/pages/index/ai' })
      break
    case 'person':
      uni.reLaunch({ url: '/pages/index/person' })
      break
  }
  currentTab.value = tab
}
</script>

<style scoped>
.container {
  padding: 30rpx;
  background-color: #f5f6fa;
  min-height: 100vh;
  padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
}

.card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.status-card {
  background: linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%);
  color: #fff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
}

.status-item {
  text-align: center;
}

.status-label {
  font-size: 24rpx;
  opacity: 0.8;
  margin-bottom: 10rpx;
  display: block;
}

.status-value {
  font-size: 36rpx;
  font-weight: bold;
}

.control-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.control-item {
  background: #fff;
  padding: 30rpx;
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.control-item.active {
  background: #1a73e8;
  color: #fff;
}

.control-icon {
  font-size: 48rpx;
  margin-bottom: 10rpx;
}

.control-label {
  font-size: 24rpx;
  margin-bottom: 10rpx;
}

.control-status {
  font-size: 28rpx;
  font-weight: bold;
}

.env-card {
  flex: 1;
  background: #fff;
  padding: 30rpx;
  border-radius: 20rpx;
  text-align: center;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.env-title {
  font-size: 24rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.env-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #1a73e8;
}

.env-tip {
  font-size: 20rpx;
  color: #999;
  margin-top: 8rpx;
}

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

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 90vw;
  max-width: 650rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx 20rpx;
  box-sizing: border-box;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.modal-close {
  font-size: 40rpx;
  color: #999;
  padding: 10rpx;
  cursor: pointer;
}

.chart-container {
  width: 100%;
  height: 875rpx;
  padding: 0;
  overflow: hidden;
  box-sizing: border-box;
  position: relative;
}

.chart {
  width: 100%;
  height: 100%;
}

.control-tip {
  font-size: 20rpx;
  color: #999;
  margin-top: 4rpx;
}

.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 6rpx solid #f3f3f3;
  border-top: 6rpx solid #1a73e8;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20rpx;
}

.loading-text {
  color: #fff;
  font-size: 28rpx;
}

.connection-status {
  position: fixed;
  top: 20rpx;
  right: 20rpx;
  display: flex;
  align-items: center;
  padding: 10rpx 20rpx;
  border-radius: 30rpx;
  background: rgba(0, 0, 0, 0.6);
  z-index: 100;
}

.connection-status.connected {
  background: rgba(26, 115, 232, 0.8);
}

.connection-status.disconnected {
  background: rgba(255, 0, 0, 0.8);
}

.status-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-right: 10rpx;
}

.connected .status-dot {
  background: #4CAF50;
  box-shadow: 0 0 10rpx #4CAF50;
}

.disconnected .status-dot {
  background: #FF5252;
  box-shadow: 0 0 10rpx #FF5252;
}

.status-text {
  color: #fff;
  font-size: 24rpx;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.city-modal-content {
  max-width: 500rpx;
  min-width: 320rpx;
  padding: 40rpx 30rpx 30rpx 30rpx;
}
.city-input-row {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}
.city-input {
  flex: 1;
  border: 1px solid #e0e0e0;
  border-radius: 10rpx;
  padding: 16rpx 20rpx;
  font-size: 26rpx;
  outline: none;
  background: #f7f8fa;
  margin-right: 16rpx;
}
.city-btn {
  background: linear-gradient(90deg, #1a73e8 0%, #0d47a1 100%);
  color: #fff;
  border: none;
  border-radius: 10rpx;
  padding: 0 32rpx;
  height: 60rpx;
  font-size: 26rpx;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.2s;
}
.city-btn:disabled {
  background: #b0c4de;
  color: #eee;
}
.city-result {
  margin-top: 20rpx;
  text-align: center;
}
.city-row {
  margin-bottom: 10rpx;
}
.city-label {
  color: #666;
  font-size: 26rpx;
}
.city-value {
  color: #1a73e8;
  font-size: 30rpx;
  font-weight: bold;
  margin-left: 8rpx;
}
.city-error {
  color: #ff5252;
  text-align: center;
  margin-top: 20rpx;
  font-size: 26rpx;
}
.alert-modal-content {
  max-width: 500rpx;
  min-width: 320rpx;
  padding: 40rpx 30rpx 30rpx 30rpx;
}
.alert-message {
  text-align: center;
  font-size: 28rpx;
  color: #ff5252;
  margin-top: 20rpx;
}
</style>
