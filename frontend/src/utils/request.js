import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router' // 引入路由，方便未登录时跳转

// 创建一个 axios 实例
const request = axios.create({
  baseURL: '/api', // 统一前缀
  timeout: 5000
})

// 请求拦截器：每次发请求前，自动加上 token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['token'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器：处理 token 失效的情况
request.interceptors.response.use(
  response => {
    return response.data // 直接返回数据，省去每次写 res.data
  },
  error => {
    // 如果是 401，说明没登录或 token 过期
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      router.push('/login')
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request