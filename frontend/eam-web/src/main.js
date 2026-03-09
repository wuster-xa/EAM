/**
 * Vue 3 主入口文件
 * 
 * 功能说明：
 * 1. 创建 Vue 应用实例
 * 2. 注册全局插件（Element Plus、Pinia、Router）
 * 3. 注册全局图标组件
 * 4. 挂载应用到 DOM
 * 
 * @author 毕业设计项目组
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import './styles/index.scss'

// 创建 Vue 应用实例
const app = createApp(App)

// 注册 Pinia 状态管理
app.use(createPinia())

// 注册 Vue Router
app.use(router)

// 注册 Element Plus（中文语言包）
app.use(ElementPlus, {
  locale: zhCn
})

// 全局注册 Element Plus 图标组件
// 使用方式：<el-icon><Edit /></el-icon>
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 挂载应用
app.mount('#app')
