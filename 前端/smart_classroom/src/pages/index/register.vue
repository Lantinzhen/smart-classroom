<template>
  <view class="register-container">
    <view class="title">用户注册</view>
    <view class="form-container">
      <view class="input-group">
        <text class="label">用户名</text>
        <input v-model="username" type="text" placeholder="请输入用户名" class="input" />
      </view>
      <view class="input-group">
        <text class="label">密码</text>
        <input v-model="password" type="password" placeholder="请输入密码" class="input" />
      </view>
      <view class="input-group">
        <text class="label">确认密码</text>
        <input v-model="confirmPassword" type="password" placeholder="请再次输入密码" class="input" />
      </view>
      <button class="register-btn" @click="handleRegister">注册</button>
      <view class="login-link" @click="goToLogin">已有账号？立即登录</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import{ BASE_URL } from '@/config/config.js'
const username = ref('')
const password = ref('')
const confirmPassword = ref('')

const handleRegister = async () => {
  if (!username.value || !password.value || !confirmPassword.value) {
    uni.showToast({
      title: '请填写完整信息',
      icon: 'none'
    })
    return
  }

  if (password.value !== confirmPassword.value) {
    uni.showToast({
      title: '两次输入的密码不一致',
      icon: 'none'
    })
    return
  }

  try {
    const res = await uni.request({
      url: `${BASE_URL}/api/user/register`,
      method: 'POST',
      data: {
        name: username.value,
        password: password.value
      }
    })

    if (res.statusCode === 200) {
      uni.showToast({
        title: '注册成功',
        icon: 'success'
      })

      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      uni.showToast({
        title: '注册失败，用户名可能已存在',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.showToast({
      title: '注册失败，请稍后重试',
      icon: 'none'
    })
  }
}

const goToLogin = () => {
  uni.reLaunch({
    url: "/pages/index/login"
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%);
}

.title {
  font-size: 40rpx;
  color: #ffffff;
  margin: 60rpx 0;
  font-weight: bold;
}

.form-container {
  width: 100%;
  padding: 0 40rpx;
}

.input-group {
  margin-bottom: 30rpx;
}

.label {
  display: block;
  color: #ffffff;
  margin-bottom: 10rpx;
  font-size: 28rpx;
}

.input {
  width: 100%;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 40rpx;
  padding: 0 30rpx;
  color: #ffffff;
  font-size: 28rpx;
}

.register-btn {
  width: 100%;
  height: 80rpx;
  background: #ffffff;
  color: #1a73e8;
  border-radius: 40rpx;
  margin-top: 40rpx;
  font-size: 32rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-btn:active {
  opacity: 0.8;
}

.login-link {
  margin-top: 20rpx;
  color: #ffffff;
  font-size: 28rpx;
  text-align: center;
  text-decoration: underline;
}
</style>