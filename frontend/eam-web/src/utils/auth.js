/**
 * Token 存储工具
 * 
 * 功能说明：
 * - 存储、获取、删除 JWT Token
 * - 使用 localStorage 持久化存储
 * 
 * @author 毕业设计项目组
 */

const TOKEN_KEY = 'eam_token'

/**
 * 获取 Token
 * @returns {string|null}
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置 Token
 * @param {string} token
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 删除 Token
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 检查是否已登录
 * @returns {boolean}
 */
export function isLoggedIn() {
  return !!getToken()
}
