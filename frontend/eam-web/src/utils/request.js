/**
 * Axios 请求封装
 * 
 * 功能说明：
 * 1. 创建 Axios 实例
 * 2. 请求拦截器：自动添加 Token
 * 3. 响应拦截器：统一处理响应和错误
 * 
 * @author 毕业设计项目组
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import router from '@/router'

// 创建 Axios 实例
const service = axios.create({
  baseURL: '/api',  // API 基础路径
  timeout: 30000,   // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

/**
 * 请求拦截器
 * 
 * 功能：
 * - 在请求头中自动添加 JWT Token
 */
service.interceptors.request.use(
  config => {
    // 获取 Token 并添加到请求头
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 
 * 功能：
 * - 统一处理响应数据
 * - 统一处理错误（401 跳转登录、其他错误提示）
 */
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // code 为 200 表示成功
    if (res.code === 200) {
      return res
    }
    
    // 处理特定错误码
    if (res.code === 401) {
      // Token 失效，清除本地 Token 并跳转登录页
      ElMessage.error('登录已过期，请重新登录')
      removeToken()
      router.push('/login')
      return Promise.reject(new Error(res.msg || '登录已过期'))
    }
    
    if (res.code === 403) {
      ElMessage.error('无权限访问该资源')
      return Promise.reject(new Error(res.msg || '无权限'))
    }
    
    // 其他错误，显示错误信息
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  error => {
    console.error('响应错误:', error)
    
    // 网络错误
    if (error.message.includes('timeout')) {
      ElMessage.error('请求超时，请稍后重试')
    } else if (error.message.includes('Network Error')) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

export default service
