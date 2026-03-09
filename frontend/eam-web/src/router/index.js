/**
 * Vue Router 路由配置
 * 
 * 路由结构：
 * - /login: 登录页
 * - /: 主布局（包含侧边栏、顶部导航）
 *   - /dashboard: 数据可视化驾驶舱
 *   - /asset/list: 资产台账管理
 *   - /asset/category: 资产分类管理
 *   - /operation/apply: 资产领用申请
 *   - /operation/approval: 审批管理
 *   - /operation/records: 操作记录
 *   - /depreciation: 折旧管理
 * 
 * @author 毕业设计项目组
 */
import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

// 静态路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据看板', icon: 'DataLine' }
      },
      {
        path: 'asset/list',
        name: 'AssetList',
        component: () => import('@/views/asset/list.vue'),
        meta: { title: '资产台账', icon: 'Document' }
      },
      {
        path: 'asset/category',
        name: 'AssetCategory',
        component: () => import('@/views/asset/category.vue'),
        meta: { title: '资产分类', icon: 'Folder' }
      },
      {
        path: 'asset/detail/:id',
        name: 'AssetDetail',
        component: () => import('@/views/asset/detail.vue'),
        meta: { title: '资产详情', hidden: true }
      },
      {
        path: 'operation/apply',
        name: 'AssetApply',
        component: () => import('@/views/operation/apply.vue'),
        meta: { title: '资产领用', icon: 'Plus' }
      },
      {
        path: 'operation/approval',
        name: 'Approval',
        component: () => import('@/views/operation/approval.vue'),
        meta: { title: '审批管理', icon: 'Checked' }
      },
      {
        path: 'operation/records',
        name: 'OperationRecords',
        component: () => import('@/views/operation/records.vue'),
        meta: { title: '操作记录', icon: 'List' }
      },
      {
        path: 'depreciation',
        name: 'Depreciation',
        component: () => import('@/views/depreciation/index.vue'),
        meta: { title: '折旧管理', icon: 'TrendCharts' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hidden: true }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 路由守卫
 * 
 * 功能：
 * 1. 设置页面标题
 * 2. 验证登录状态
 */
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - EAM系统` : 'EAM系统'
  
  // 白名单路由（无需登录）
  const whiteList = ['/login', '/404']
  
  if (whiteList.includes(to.path)) {
    next()
  } else {
    // 检查是否有 Token
    const token = getToken()
    if (token) {
      next()
    } else {
      // 未登录，跳转到登录页
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router
