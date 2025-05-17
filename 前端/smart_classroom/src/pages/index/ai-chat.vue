<template>
  <view class="container">
    <view class="chat-header">
      <text class="chat-title">AI 语音对话</text>
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

    <!-- 文本输入区域 -->
    <view class="input-container">
      <input class="text-input" 
             v-model="inputMessage"
             placeholder="请输入您的问题"
             @confirm="sendMessage" />
      <button class="send-btn" @tap="sendMessage">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { BASE_URL } from '@/config/config.js'

// 聊天模式数据
const chatMessages = ref([
  { type: 'ai', content: '你好！我是智慧教室AI助手，请输入您的问题。' }
])
const scrollTop = ref(0)
const inputMessage = ref('')

// 语音合成相关
let innerAudioContext = null
let isPlaying = ref(false)
const audioCache = new Map()

// API配置
const API_KEY = "regXNHcWPwDnv5D4PnNFaROQZ6" // 需要替换为实际的API密钥
const API_URL = "https://api.tjit.net/api/ai/audio/speech"

// 语音合成
const textToSpeech = async (text) => {
  try {
    console.log('准备合成的文本:', text)
    
    // 文本预处理：只去除英文和特殊符号
    const processedText = text.replace(/[a-zA-Z]/g, '')  // 去除英文字母
                             .replace(/[^\u4e00-\u9fa5，。！？、：；""''（）\s]/g, '')  // 只保留中文、标点和空格
                             .replace(/\s+/g, ' ')  // 将多个空格替换为单个空格
                             .trim()  // 去除首尾空格
    
    console.log('处理后的文本:', processedText)
    
    // 检查缓存
    if (audioCache.has(processedText)) {
      console.log('使用缓存的音频数据')
      const cachedAudio = audioCache.get(processedText)
      playAudioWithContext(cachedAudio)
      return
    }

    // 准备请求参数
    const params = {
      key: API_KEY,
      text: processedText,  // 使用处理后的文本
      voice: 'zh-CN-XiaoxiaoNeural',
      format: 'mp3',
      type: 'speech',
      speed: '1.0',
      model: 'tts-1-hd'
    }

    console.log('请求参数:', params)

    // 发送请求
    const response = await new Promise((resolve, reject) => {
      uni.request({
        url: API_URL,
        method: 'GET',
        data: params,
        responseType: 'arraybuffer',
        timeout: 30000,  // 增加超时时间到30秒
        success: (res) => {
          resolve(res)
        },
        fail: (err) => {
          console.error('请求失败:', err)
          // 如果是超时错误，给出更友好的提示
          if (err.errMsg && err.errMsg.includes('timeout')) {
            uni.showToast({
              title: '语音合成超时，请稍后重试',
              icon: 'none',
              duration: 2000
            })
          }
          reject(err)
        }
      })
    })

    if (response.statusCode === 200) {
      const audioData = response.data
      if (audioData && typeof audioData.byteLength === 'number' && audioData.byteLength > 0) {
        console.log('收到音频数据，长度:', audioData.byteLength)
        try {
          // 将ArrayBuffer转换为base64
          const base64 = uni.arrayBufferToBase64(audioData)
          console.log('音频数据已转换为base64，长度:', base64.length)
          
          // 使用底层的文件处理方式
          const base64ToFile = (base64Str, fileName, callback) => {
            var index = base64Str.indexOf(',')
            var base64Str = base64Str.slice(index + 1, base64Str.length)

            plus.io.requestFileSystem(plus.io.PRIVATE_DOC, function(fs) {
              fs.root.getFile(fileName, {
                create: true
              }, function(entry) {
                var fullPath = entry.fullPath;
                let platform = uni.getSystemInfoSync().platform
                if (platform == 'android') {
                  var Base64 = plus.android.importClass("android.util.Base64");
                  var FileOutputStream = plus.android.importClass("java.io.FileOutputStream");
                  try {
                    var out = new FileOutputStream(fullPath);
                    var bytes = Base64.decode(base64Str, Base64.DEFAULT);
                    out.write(bytes);
                    out.close();
                    callback && callback(entry.toLocalURL());
                  } catch (e) {
                    console.error('Android文件写入失败:', e.message);
                    uni.showToast({ 
                      title: '音频处理失败，请重试',
                      icon: 'none',
                      duration: 2000
                    })
                  }
                } else if (platform == 'ios') {
                  var NSData = plus.ios.importClass('NSData');
                  var nsData = new NSData();
                  nsData = nsData.initWithBase64EncodedStringoptions(base64Str, 0);
                  if (nsData) {
                    nsData.plusCallMethod({
                      writeToFile: fullPath,
                      atomically: true
                    });
                    plus.ios.deleteObject(nsData);
                  }
                  callback && callback(entry.toLocalURL());
                }
              }, (err) => {
                console.error('获取文件失败:', err);
                uni.showToast({ 
                  title: '音频处理失败，请重试',
                  icon: 'none',
                  duration: 2000
                })
              })
            }, (err) => {
              console.error('请求文件系统失败:', err);
              uni.showToast({ 
                title: '音频处理失败，请重试',
                icon: 'none',
                duration: 2000
              })
            })
          }

          // 生成文件名并保存文件
          const fileName = `temp_${Date.now()}.mp3`;
          base64ToFile(base64, fileName, (filePath) => {
            console.log('音频文件保存成功，路径:', filePath);
            
            // 销毁旧的音频实例
            if (innerAudioContext) {
              try {
                innerAudioContext.pause()
                innerAudioContext.destroy()
                innerAudioContext = null
              } catch (e) {
                console.error('销毁旧音频实例失败:', e)
              }
            }
            
            // 创建新的音频实例
            try {
              innerAudioContext = uni.createInnerAudioContext()
              innerAudioContext.autoplay = true
              innerAudioContext.src = filePath
              
              innerAudioContext.onPlay(() => {
                console.log('开始播放')
              })
              
              innerAudioContext.onWaiting(() => {
                console.log("音频加载中")
              })
              
              innerAudioContext.onError((res) => {
                console.error('播放错误:', res.errMsg, res.errCode)
                uni.showToast({ 
                  title: '音频播放失败，请重试',
                  icon: 'none',
                  duration: 2000
                })
              })
              
              innerAudioContext.onEnded(() => {
                console.log('播放结束')
                if (innerAudioContext) {
                  try {
                    innerAudioContext.destroy()
                    innerAudioContext = null
                  } catch (e) {
                    console.error('销毁音频实例失败:', e)
                  }
                }
              })
            } catch (err) {
              console.error('创建音频上下文失败:', err)
              uni.showToast({ 
                title: '音频播放失败，请重试',
                icon: 'none',
                duration: 2000
              })
            }
          })
        } catch (err) {
          console.error('音频处理失败:', err)
          uni.showToast({ 
            title: '音频处理失败，请重试',
            icon: 'none',
            duration: 2000
          })
        }
      } else {
        console.error('API未返回有效音频数据，实际内容:', audioData)
        uni.showToast({ title: 'API未返回音频', icon: 'none' })
        throw new Error('API未返回有效音频数据')
      }
    } else {
      console.error('API响应错误:', response)
      throw new Error('语音合成失败')
    }
  } catch (error) {
    console.error('语音合成失败:', error)
    isPlaying.value = false
    uni.showToast({
      title: '语音合成失败',
      icon: 'none'
    })
  }
}

// 使用音频上下文播放
const playAudioWithContext = (audioPath) => {
  return new Promise((resolve, reject) => {
    try {
      // 创建音频上下文
      if (innerAudioContext) {
        innerAudioContext.destroy()
      }
      innerAudioContext = uni.createInnerAudioContext()
      
      // 设置音频源
      innerAudioContext.src = audioPath
      innerAudioContext.autoplay = true
      
      // 添加事件监听
      innerAudioContext.onCanplay(() => {
        console.log('音频可以播放')
      })
      
      innerAudioContext.onPlay(() => {
        console.log('开始播放音频')
        isPlaying.value = true
      })
      
      innerAudioContext.onEnded(() => {
        console.log('音频播放结束')
        isPlaying.value = false
        resolve()
      })
      
      innerAudioContext.onError((res) => {
        console.error('音频播放错误:', res)
        isPlaying.value = false
        // 尝试使用备用播放方式
        try {
          const audio = new Audio(audioPath)
          audio.oncanplay = () => {
            console.log('备用播放方式：音频可以播放')
            audio.play()
          }
          audio.onended = () => {
            console.log('备用播放方式：音频播放结束')
            isPlaying.value = false
            resolve()
          }
          audio.onerror = (err) => {
            console.error('备用播放方式错误:', err)
            uni.showToast({
              title: '音频播放失败',
              icon: 'none'
            })
            reject(err)
          }
        } catch (err) {
          console.error('备用播放方式初始化失败:', err)
          uni.showToast({
            title: '音频播放失败',
            icon: 'none'
          })
          reject(err)
        }
      })
      
    } catch (error) {
      console.error('音频播放失败:', error)
      isPlaying.value = false
      uni.showToast({
        title: '音频播放失败',
        icon: 'none'
      })
      reject(error)
    }
  })
}

// 清理音频缓存
const clearAudioCache = () => {
  try {
    audioCache.clear()
    console.log('音频缓存已清理')
  } catch (error) {
    console.error('清理音频缓存失败:', error)
  }
}

// 发送聊天消息
const sendMessage = async () => {
  const userMessage = inputMessage.value.trim()
  if (!userMessage) return
  
  chatMessages.value.push({ type: 'user', content: userMessage })
  inputMessage.value = ''
  
  // 自动滚动到底部
  scrollToBottom()
  
  try {
    // 在发送给AI的消息中添加额外提示
    const enhancedMessage = `${userMessage}。回答偏口语化，不要使用专业术语、颜文字，回答字数尽量简单一些。`
    const response = await uni.request({
      url: `${BASE_URL}/api/askDeepSeek?question=${encodeURIComponent(enhancedMessage)}`,
      method: 'GET'
    })

    if (response.statusCode === 200) {
      let aiReply = ''
      if (typeof response.data === 'string') {
        aiReply = response.data
      } else if (response.data && response.data.answer) {
        aiReply = response.data.answer
      }
      console.log(aiReply)
      if (aiReply) {
        // 只使用语音合成播放AI回复，不显示文本
        textToSpeech(aiReply)
      } else {
        textToSpeech('AI暂时无法回答，请稍后再试。')
      }
    } else {
      textToSpeech('AI暂时无法回答，请稍后再试。')
    }
  } catch (error) {
    console.error('AI回复失败:', error)
    textToSpeech('抱歉，我现在无法回答，请稍后再试。')
  } finally {
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

// 在组件卸载时清理
onUnmounted(() => {
  if (innerAudioContext) {
    innerAudioContext.destroy()
  }
  clearAudioCache()
})
</script>

<style scoped>
.container {
  padding: 30rpx;
  background-color: #f5f6fa;
  min-height: 100vh;
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
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 120rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  height: calc(100vh - 300rpx);
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

.input-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: #fff;
  display: flex;
  align-items: center;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.text-input {
  flex: 1;
  height: 80rpx;
  background: #f5f6fa;
  border-radius: 40rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
  margin-right: 20rpx;
}

.send-btn {
  width: 120rpx;
  height: 80rpx;
  background: #1a73e8;
  color: #fff;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}
</style> 