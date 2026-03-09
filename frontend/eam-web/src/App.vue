<template>
  <!-- 
    根组件
    使用 router-view 渲染当前路由对应的组件
  -->
  <router-view />
</template>

<script setup>
/**
 * App 根组件
 * 
 * 作为整个应用的容器，通过 router-view 渲染子组件
 */
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}
</style>
