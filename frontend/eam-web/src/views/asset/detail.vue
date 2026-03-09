<template>
  <div class="asset-detail">
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <span>资产详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="资产编码">{{ asset.assetCode }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ asset.assetName }}</el-descriptions-item>
        <el-descriptions-item label="资产分类">{{ asset.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ asset.brand }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ asset.model }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(asset.status)">{{ getStatusName(asset.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="资产原值">¥{{ asset.originalValue?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="当前净值">¥{{ asset.netValue?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="累计折旧">¥{{ asset.accumulatedDepreciation?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="残值率">{{ asset.residualRate }}%</el-descriptions-item>
        <el-descriptions-item label="购置日期">{{ asset.purchaseDate }}</el-descriptions-item>
        <el-descriptions-item label="存放位置">{{ asset.location }}</el-descriptions-item>
        <el-descriptions-item label="使用部门">{{ asset.deptName }}</el-descriptions-item>
        <el-descriptions-item label="使用人">{{ asset.userName }}</el-descriptions-item>
        <el-descriptions-item label="UUID" :span="2">{{ asset.uuid }}</el-descriptions-item>
      </el-descriptions>
      
      <div class="qrcode-section" v-if="asset.qrCodeBase64">
        <h4>资产二维码</h4>
        <img :src="asset.qrCodeBase64" alt="二维码" class="qrcode-img" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAssetById } from '@/api/asset'

const route = useRoute()
const router = useRouter()
const asset = ref({})

const getStatusName = (status) => {
  const map = { 0: '闲置', 1: '在用', 2: '维修中', 3: '报废' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return map[status] || 'info'
}

const goBack = () => router.back()

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await getAssetById(id)
    asset.value = res.data
  } catch (error) {
    console.error('加载资产详情失败:', error)
  }
})
</script>

<style lang="scss" scoped>
.asset-detail {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .qrcode-section {
    margin-top: 30px;
    text-align: center;
    
    h4 {
      margin-bottom: 15px;
    }
    
    .qrcode-img {
      width: 200px;
      height: 200px;
    }
  }
}
</style>
