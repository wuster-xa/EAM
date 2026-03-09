<template>
  <div class="depreciation-container">
    <el-card shadow="never">
      <template #header>
        <el-button type="primary" @click="handleExecute">
          <el-icon><VideoPlay /></el-icon>
          执行折旧计算
        </el-button>
      </template>
      
      <el-table :data="depreciationList" border stripe>
        <el-table-column prop="assetName" label="资产名称" min-width="150" />
        <el-table-column prop="originalValue" label="原值" width="120" align="right">
          <template #default="{ row }">¥{{ row.originalValue?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="depreciationAmount" label="本月折旧" width="120" align="right">
          <template #default="{ row }">¥{{ row.depreciationAmount?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="netValueAfter" label="折旧后净值" width="120" align="right">
          <template #default="{ row }">¥{{ row.netValueAfter?.toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="depreciationYear" label="折旧年度" width="100" />
        <el-table-column prop="depreciationMonth" label="折旧月份" width="100" />
        <el-table-column prop="executeTime" label="执行时间" width="180" />
      </el-table>
      
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="10"
        :total="total"
        layout="total, prev, pager, next"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const pageNum = ref(1)
const total = ref(0)
const depreciationList = ref([])

const handleExecute = () => {
  ElMessage.info('折旧计算由定时任务自动执行')
}

onMounted(() => {
  // 模拟数据
  depreciationList.value = [
    { assetName: '联想ThinkPad笔记本', originalValue: 5999, depreciationAmount: 158.31, netValueAfter: 5683.69, depreciationYear: 2026, depreciationMonth: 3, executeTime: '2026-03-01 00:05:00' },
    { assetName: '戴尔显示器', originalValue: 1999, depreciationAmount: 52.75, netValueAfter: 1894.25, depreciationYear: 2026, depreciationMonth: 3, executeTime: '2026-03-01 00:05:00' }
  ]
  total.value = 2
})
</script>
