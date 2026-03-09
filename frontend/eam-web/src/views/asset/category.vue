<template>
  <div class="category-container">
    <el-card shadow="never">
      <template #header>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增分类
        </el-button>
      </template>
      
      <el-table :data="categoryList" border stripe row-key="id">
        <el-table-column prop="categoryName" label="分类名称" />
        <el-table-column prop="categoryCode" label="分类编码" width="120" />
        <el-table-column prop="depreciationYears" label="折旧年限(年)" width="120" />
        <el-table-column prop="residualRate" label="残值率(%)" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const categoryList = ref([])

const handleAdd = () => ElMessage.info('新增功能开发中')
const handleEdit = () => ElMessage.info('编辑功能开发中')
const handleDelete = () => ElMessage.info('删除功能开发中')

onMounted(() => {
  // 模拟数据
  categoryList.value = [
    { id: 1, categoryName: '电子设备', categoryCode: 'ELECTRONIC', depreciationYears: 3, residualRate: 5, status: 1 },
    { id: 2, categoryName: '办公家具', categoryCode: 'FURNITURE', depreciationYears: 5, residualRate: 5, status: 1 },
    { id: 3, categoryName: '运输工具', categoryCode: 'VEHICLE', depreciationYears: 10, residualRate: 5, status: 1 },
    { id: 4, categoryName: '机械设备', categoryCode: 'MACHINE', depreciationYears: 10, residualRate: 5, status: 1 }
  ]
})
</script>
