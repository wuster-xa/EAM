import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

/**
 * Vite 配置文件
 * 
 * 配置说明：
 * - plugins: Vue 3 插件
 * - resolve.alias: 路径别名，@ 指向 src 目录
 * - server: 开发服务器配置
 */
export default defineConfig({
  plugins: [vue()],
  
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  
  server: {
    port: 3000,
    // 代理配置，解决跨域问题
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  
  // 构建配置
  build: {
    outDir: 'dist',
    sourcemap: false
  }
})
