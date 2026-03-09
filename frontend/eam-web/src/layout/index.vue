<template>
  <!-- 
    主布局组件
    
    结构：
    - 左侧：侧边栏导航菜单
    - 右侧：顶部导航栏 + 主内容区
  -->
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <img src="@/assets/logo.svg" alt="EAM" class="logo-img" />
        <span v-show="!isCollapse" class="logo-text">EAM系统</span>
      </div>
      
      <!-- 导航菜单 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <template #title>数据看板</template>
        </el-menu-item>
        
        <el-sub-menu index="asset">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>资产管理</span>
          </template>
          <el-menu-item index="/asset/list">资产台账</el-menu-item>
          <el-menu-item index="/asset/category">资产分类</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="operation">
          <template #title>
            <el-icon><Operation /></el-icon>
            <span>流转管理</span>
          </template>
          <el-menu-item index="/operation/apply">资产领用</el-menu-item>
          <el-menu-item index="/operation/approval">审批管理</el-menu-item>
          <el-menu-item index="/operation/records">操作记录</el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/depreciation">
          <el-icon><TrendCharts /></el-icon>
          <template #title>折旧管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <!-- 折叠按钮 -->
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <!-- 面包屑导航 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.meta?.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 用户信息下拉菜单 -->
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="User" />
              <span class="username">{{ userStore.realName || userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容 -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
/**
 * 主布局组件
 * 
 * 功能：
 * - 侧边栏导航菜单
 * - 顶部导航栏
 * - 用户信息展示
 * - 登出功能
 */
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

// 路由
const route = useRoute()
const router = useRouter()

// 用户状态
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 当前激活菜单
const activeMenu = computed(() => route.path)

// 面包屑导航
const breadcrumbs = computed(() => {
  return route.matched.filter(item => item.meta?.title)
})

/**
 * 切换侧边栏折叠
 */
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

/**
 * 处理下拉菜单命令
 */
const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
    }).catch(() => {})
  } else if (command === 'profile') {
    // 跳转个人中心
    router.push('/profile')
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  width: 100%;
  height: 100%;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #263445;
    
    .logo-img {
      width: 32px;
      height: 32px;
    }
    
    .logo-text {
      margin-left: 10px;
      font-size: 18px;
      font-weight: bold;
      color: #fff;
    }
  }
  
  .sidebar-menu {
    border-right: none;
    background-color: #304156;
    
    &:not(.el-menu--collapse) {
      width: 220px;
    }
  }
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  
  .header-left {
    display: flex;
    align-items: center;
    
    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      margin-right: 15px;
      
      &:hover {
        color: #409eff;
      }
    }
  }
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      
      .username {
        margin: 0 8px;
        font-size: 14px;
      }
    }
  }
}

.main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
