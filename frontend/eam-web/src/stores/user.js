/**
 * 用户状态管理
 * 
 * 使用 Pinia 管理用户状态：
 * - 用户信息
 * - Token
 * - 登录/登出
 * 
 * @author 毕业设计项目组
 */
import { defineStore } from 'pinia'
import { login, getCurrentUser } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'
import router from '@/router'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: null,
    roles: []
  }),

  getters: {
    // 是否已登录
    isLoggedIn: (state) => !!state.token,
    // 用户名
    username: (state) => state.userInfo?.username || '',
    // 真实姓名
    realName: (state) => state.userInfo?.realName || '',
    // 角色编码
    roleCode: (state) => state.userInfo?.roleCode || '',
    // 是否是管理员
    isAdmin: (state) => ['SUPER_ADMIN', 'ASSET_MANAGER'].includes(state.userInfo?.roleCode)
  },

  actions: {
    /**
     * 用户登录
     * @param {Object} loginForm - 登录表单 { username, password }
     */
    async login(loginForm) {
      try {
        const res = await login(loginForm)
        const { token, ...userInfo } = res.data
        
        // 存储 Token
        this.token = token
        setToken(token)
        
        // 存储用户信息
        this.userInfo = userInfo
        this.roles = [userInfo.roleCode]
        
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },

    /**
     * 获取当前用户信息
     */
    async getUserInfo() {
      try {
        const res = await getCurrentUser()
        this.userInfo = res.data
        this.roles = [res.data.roleCode]
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },

    /**
     * 用户登出
     */
    logout() {
      this.token = ''
      this.userInfo = null
      this.roles = []
      removeToken()
      router.push('/login')
    },

    /**
     * 清除用户信息
     */
    clearUserInfo() {
      this.token = ''
      this.userInfo = null
      this.roles = []
      removeToken()
    }
  }
})
