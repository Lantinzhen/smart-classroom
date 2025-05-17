<template>
    <view class="container">
        <!-- 用户信息卡片 -->
        <view class="user-card">
            <view class="avatar-wrapper">
                <image class="avatar" src="/static/default-avatar.png" mode="scaleToFill"></image>
            </view>
            <view class="user-info">
                <text class="username">{{ userInfo.username }}</text>
            </view>
        </view>

        <!-- 功能列表 -->
        <view class="menu-list">
            <view class="menu-item" @click="handleMenuClick('account')">
                <view class="menu-item-left">
                    <text class="menu-icon">👤</text>
                    <text class="menu-text">账号管理</text>
                </view>
                <text class="menu-arrow">></text>
            </view>

            <view class="menu-item" @click="handleMenuClick('security')">
                <view class="menu-item-left">
                    <text class="menu-icon">🔒</text>
                    <text class="menu-text">安全设置</text>
                </view>
                <text class="menu-arrow">></text>
            </view>

            <view class="menu-item" @click="handleMenuClick('notification')">
                <view class="menu-item-left">
                    <text class="menu-icon">🔔</text>
                    <text class="menu-text">消息通知</text>
                </view>
                <text class="menu-arrow">></text>
            </view>

            <view class="menu-item" @click="handleMenuClick('about')">
                <view class="menu-item-left">
                    <text class="menu-icon">ℹ️</text>
                    <text class="menu-text">关于我们</text>
                </view>
                <text class="menu-arrow">></text>
            </view>
        </view>

        <!-- 退出登录按钮 -->
        <button class="logout-btn" @click="handleLogout">退出登录</button>

        <!-- 底部导航栏 -->
        <view class="tab-bar">
            <view class="tab-item" @click="switchTab('monitor')">
                <text class="tab-icon">📺</text>
                <text class="tab-text">监控</text>
            </view>
            <view class="tab-item" @click="switchTab('ai')">
                <text class="tab-icon">🤖</text>
                <text class="tab-text">AI</text>
            </view>
            <view class="tab-item active">
                <text class="tab-icon">👤</text>
                <text class="tab-text">我的</text>
            </view>
        </view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const userInfo = ref({
    username: ''
})

// 获取用户信息
onMounted(() => {
    const currentUser = uni.getStorageSync('currentUser')
    if (currentUser) {
        userInfo.value.username = currentUser.username
    }
})

// 处理菜单点击
const handleMenuClick = (type) => {
    switch (type) {
        case 'account':
            uni.showToast({
                title: '账号管理功能开发中',
                icon: 'none'
            })
            break
        case 'security':
            uni.showToast({
                title: '安全设置功能开发中',
                icon: 'none'
            })
            break
        case 'notification':
            uni.showToast({
                title: '消息通知功能开发中',
                icon: 'none'
            })
            break
        case 'about':
            uni.showToast({
                title: '关于我们功能开发中',
                icon: 'none'
            })
            break
    }
}

// 处理退出登录
const handleLogout = () => {
    uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
            if (res.confirm) {
                uni.clearStorageSync()
                uni.reLaunch({
                    url: '/pages/index/login'
                })
            }
        }
    })
}

// 切换底部导航
const switchTab = (tab) => {
    switch (tab) {
        case 'monitor':
            uni.reLaunch({ url: '/pages/index/home' })
            break
        case 'ai':
            uni.reLaunch({ url: '/pages/index/ai' })
            break
    }
}
</script>

<style scoped>
.container {
    min-height: 100vh;
    background-color: #f5f6fa;
    padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
}

.user-card {
    background: linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%);
    padding: 60rpx 40rpx;
    display: flex;
    align-items: center;
    margin: 20rpx;
    border-radius: 20rpx;
}

.avatar-wrapper {
    width: 140rpx;
    height: 140rpx;
    border-radius: 50%;
    overflow: hidden;
    background-color: #fff;
}

.avatar {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.user-info {
    margin-left: 30rpx;
}

.username {
    font-size: 40rpx;
    color: #ffffff;
    font-weight: 500;
}

.menu-list {
    background: #ffffff;
    margin: 20rpx;
    border-radius: 20rpx;
    padding: 0 20rpx;
}

.menu-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx 0;
    border-bottom: 1rpx solid #f5f6fa;
}

.menu-item:last-child {
    border-bottom: none;
}

.menu-item-left {
    display: flex;
    align-items: center;
}

.menu-icon {
    font-size: 40rpx;
    margin-right: 20rpx;
}

.menu-text {
    font-size: 28rpx;
    color: #333;
}

.menu-arrow {
    color: #999;
    font-size: 28rpx;
}

.logout-btn {
    margin: 40rpx 20rpx;
    background: #ff6b6b;
    color: #ffffff;
    border-radius: 45rpx;
    font-size: 32rpx;
    padding: 20rpx 0;
    border: none;
}

.logout-btn:active {
    opacity: 0.9;
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
</style>