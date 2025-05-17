// 获取存储的IP地址
const getBackendIP = () => {
    const savedIP = uni.getStorageSync('backendIP')
    return savedIP
}

const BASE_URL = `http://${getBackendIP()}:8088`
const WS = `${getBackendIP()}:8088`

export { BASE_URL, WS, getBackendIP }