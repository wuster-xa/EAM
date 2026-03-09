<template>
  <!-- 
    登录页面
    
    功能：
    1. 用户名密码登录
    2. 登录成功后存储 Token 并跳转首页
  -->
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>企业固定资产管理系统</h1>
        <p>Enterprise Asset Management System</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>默认账号：admin / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 登录页面
 * 
 * 功能：
 * 1. 用户名密码登录
 * 2. 登录成功后存储 Token
 * 3. 跳转到首页或重定向页面
 */
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 登录表单引用
const loginFormRef = ref(null)

// 加载状态
const loading = ref(false)

// 登录表单
const loginForm = reactive({
  username: 'admin',
  password: '123456'
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

/**
 * 处理登录
 */
const handleLogin = async () => {
  // 表单验证
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  
  try {
    // 调用登录接口
    await userStore.login(loginForm)
    
    ElMessage.success('登录成功')
    
    // 跳转到重定向页面或首页
    const redirect = route.query.redirect || '/'
    router.push(redirect)
    
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .login-box {
    width: 400px;
    padding: 40px;
    background-color: #fff;
    border-radius: 10px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
    
    .login-header {
      text-align: center;
      margin-bottom: 30px;
      
      h1 {
        font-size: 24px;
        color: #303133;
        margin-bottom: 10px;
      }
      
      p {
        font-size: 14px;
        color: #909399;
      }
    }
    
    .login-form {
      .login-btn {
        width: 100%;
      }
    }
    
    .login-footer {
      text-align: center;
      margin-top: 20px;
      
      p {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}
</style>
