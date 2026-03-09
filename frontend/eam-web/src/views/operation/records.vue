<template>
  <!-- 
    操作记录页面
    
    功能：
    1. 展示所有资产操作记录
    2. 支持按资产、操作类型筛选
  -->
  <div class="records-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="资产名称">
          <el-input v-model="queryParams.assetName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="queryParams.operateType" placeholder="请选择" clearable>
            <el-option label="入库" :value="1" />
            <el-option label="领用" :value="2" />
            <el-option label="退库" :value="3" />
            <el-option label="调拨" :value="4" />
            <el-option label="维修" :value="5" />
            <el-option label="报废" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRecordList">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 记录列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="assetName" label="资产名称" min-width="150" />
        <el-table-column prop="operateType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperateTypeStyle(row.operateType)">
              {{ getOperateTypeName(row.operateType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fromStatus" label="变更前状态" width="100">
          <template #default="{ row }">
            {{ getStatusName(row.fromStatus) }}
          </template>
        </el-table-column>
        <el-table-column prop="toStatus" label="变更后状态" width="100">
          <template #default="{ row }">
            {{ getStatusName(row.toStatus) }}
          </template>
        </el-table-column>
        <el-table-column prop="fromDeptName" label="原部门" width="120" />
        <el-table-column prop="toDeptName" label="目标部门" width="120" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="operateTime" label="操作时间" width="180" />
        <el-table-column prop="description" label="备注" min-width="200" show-overflow-tooltip />
      </el-table>
      
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadRecordList"
        @current-change="loadRecordList"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { queryRecordPage } from '@/api/operation'
import dayjs from 'dayjs'

const loading = ref(false)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  assetId: null,
  operateType: null
})
const tableData = ref([])
const total = ref(0)

const getOperateTypeName = (type) => {
  const map = { 1: '入库', 2: '领用', 3: '退库', 4: '调拨', 5: '维修', 6: '报废' }
  return map[type] || '未知'
}

const getOperateTypeStyle = (type) => {
  const map = { 1: 'info', 2: 'success', 3: 'warning', 4: '', 5: 'warning', 6: 'danger' }
  return map[type] || 'info'
}

const getStatusName = (status) => {
  const map = { 0: '闲置', 1: '在用', 2: '维修中', 3: '报废' }
  return map[status] || '-'
}

const loadRecordList = async () => {
  loading.value = true
  try {
    const res = await queryRecordPage(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryParams.assetId = null
  queryParams.operateType = null
  loadRecordList()
}

onMounted(() => loadRecordList())
</script>

<style lang="scss" scoped>
.records-container {
  .search-card {
    margin-bottom: 15px;
  }
  
  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>
