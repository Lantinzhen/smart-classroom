<template>
    <view class="login-container">
        <view class="logo">
            <image src="/static/logo.png" mode="aspectFit"></image>
        </view>
        <view class="title">智慧教室</view>
        <view class="form-container">
            <view class="input-group">
                <text class="label">后端IP地址</text>
                <input v-model="backendIP" type="text" placeholder="请输入后端IP地址" class="input" />
            </view>
            <view class="input-group">
                <text class="label">用户名</text>
                <input v-model="username" type="text" placeholder="请输入用户名" class="input" />
            </view>
            <view class="input-group">
                <text class="label">密码</text>
                <input v-model="password" type="password" placeholder="请输入密码" class="input" />
            </view>
            <button class="login-btn" @click="handleLogin">登录</button>
            <view class="register-link" @click="goToRegister">还没有账号？立即注册</view>
        </view>
    </view>
</template>

<script setup>
import { ref } from 'vue'

const username = ref('')
const password = ref('')
const backendIP = ref('')

const handleLogin = async () => {
    if (!username.value || !password.value) {
        uni.showToast({
            title: '请输入用户名和密码',
            icon: 'none'
        })
        return
    }

    if (!backendIP.value) {
        uni.showToast({
            title: '请输入后端IP地址',
            icon: 'none'
        })
        return
    }

    // 保存IP地址到本地存储
    uni.setStorageSync('backendIP', backendIP.value)
    console.log('backendIP: ' + backendIP.value)
    try {
        const res = await uni.request({
            url: `http://${backendIP.value}:8088/api/user/login`,
            method: 'POST',
            data: {
                name: username.value,
                password: password.value
            }
        })
        
        if (res.statusCode === 200) {
            uni.setStorageSync('isLoggedIn', true)
            uni.setStorageSync('currentUser', {
                username: username.value
            })
            
            uni.showToast({
                title: '登录成功',
                icon: 'success'
            })
            
            setTimeout(() => {
                uni.reLaunch({
                    url: '/pages/index/home',
                })
            }, 1500)
        } else {
            uni.showToast({
                title: '用户名或密码错误',
                icon: 'none'
            })
        }
    } catch (error) {
        uni.showToast({
            title: '登录失败，请稍后重试',
            icon: 'none'
        })
    }
}

const goToRegister = () => {
    uni.reLaunch({
        url: '/pages/index/register',
    })
}
</script>

<style scoped>
.login-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40rpx;
    min-height: 100vh;
    background: linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%);
}

.logo {
    width: 200rpx;
    height: 200rpx;
    margin: 60rpx 0;
}

.logo image {
    width: 100%;
    height: 100%;
}

.title {
    font-size: 40rpx;
    color: #ffffff;
    margin-bottom: 60rpx;
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

.login-btn {
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

.login-btn:active {
    opacity: 0.8;
}

.register-link {
    margin-top: 20rpx;
    color: #ffffff;
    font-size: 28rpx;
    text-align: center;
    text-decoration: underline;
}
</style>
