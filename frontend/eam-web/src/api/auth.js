/**
 * 认证相关 API
 * 
 * 接口列表：
 * - login: 用户登录
 * - getCurrentUser: 获取当前用户信息
 * - logout: 退出登录
 * 
 * @author 毕业设计项目组
 */
import request from '@/utils/request'

/**
 * 用户登录
 * 
 * @param {Object} data - 登录参数 { username, password }
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户信息
 * 
 * @returns {Promise}
 */
export function getCurrentUser() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

/**
 * 退出登录
 * 
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}
