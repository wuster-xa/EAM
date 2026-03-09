<template>
  <!-- 
    资产台账管理页面
    
    功能：
    1. 高级搜索栏（支持按部门、状态、分类筛选）
    2. 资产列表表格
    3. 查看二维码弹窗（支持打印）
    4. 新增/编辑资产
  -->
  <div class="asset-list-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="资产名称">
          <el-input v-model="queryParams.assetName" placeholder="请输入资产名称" clearable />
        </el-form-item>
        <el-form-item label="资产编码">
          <el-input v-model="queryParams.assetCode" placeholder="请输入资产编码" clearable />
        </el-form-item>
        <el-form-item label="资产状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="闲置" :value="0" />
            <el-option label="在用" :value="1" />
            <el-option label="维修中" :value="2" />
            <el-option label="报废" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="存放位置">
          <el-input v-model="queryParams.location" placeholder="请输入存放位置" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 操作栏 -->
    <el-card shadow="never" class="toolbar-card">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增资产
      </el-button>
      <el-button type="success" @click="handleExport">
        <el-icon><Download /></el-icon>
        导出Excel
      </el-button>
    </el-card>
    
    <!-- 资产列表表格 -->
    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="assetCode" label="资产编码" width="120" />
        <el-table-column prop="assetName" label="资产名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="资产分类" width="120" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="model" label="型号" width="120" show-overflow-tooltip />
        <el-table-column prop="originalValue" label="原值(元)" width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.originalValue) }}
          </template>
        </el-table-column>
        <el-table-column prop="netValue" label="净值(元)" width="120" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.netValue) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="存放位置" width="150" show-overflow-tooltip />
        <el-table-column prop="deptName" label="使用部门" width="120" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewQrCode(row)">
              <el-icon><Grid /></el-icon>
              二维码
            </el-button>
            <el-button type="primary" link @click="handleDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
    
    <!-- 二维码弹窗 -->
    <el-dialog
      v-model="qrCodeDialogVisible"
      title="资产二维码"
      width="400px"
      center
    >
      <div class="qrcode-container">
        <div class="qrcode-info">
          <p><strong>资产名称：</strong>{{ currentAsset.assetName }}</p>
          <p><strong>资产编码：</strong>{{ currentAsset.assetCode }}</p>
          <p><strong>UUID：</strong>{{ currentAsset.uuid }}</p>
        </div>
        <div class="qrcode-image">
          <img v-if="qrCodeBase64" :src="qrCodeBase64" alt="二维码" />
          <el-empty v-else description="二维码加载中..." />
        </div>
      </div>
      <template #footer>
        <el-button @click="qrCodeDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handlePrintQrCode">
          <el-icon><Printer /></el-icon>
          打印二维码
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 资产台账管理页面
 * 
 * 功能：
 * 1. 高级搜索（按部门、状态、分类筛选）
 * 2. 资产列表展示
 * 3. 二维码查看与打印
 * 4. 新增/编辑/删除资产
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { queryAssetPage, getAssetById, deleteAsset, generateQrCode } from '@/api/asset'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  assetName: '',
  assetCode: '',
  status: null,
  location: ''
})

// 表格数据
const tableData = ref([])
const total = ref(0)

// 二维码弹窗
const qrCodeDialogVisible = ref(false)
const currentAsset = ref({})
const qrCodeBase64 = ref('')

/**
 * 格式化金额
 */
const formatMoney = (value) => {
  if (!value) return '0.00'
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

/**
 * 获取状态名称
 */
const getStatusName = (status) => {
  const statusMap = {
    0: '闲置',
    1: '在用',
    2: '维修中',
    3: '报废'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取状态标签类型
 */
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return typeMap[status] || 'info'
}

/**
 * 加载资产列表
 */
const loadAssetList = async () => {
  loading.value = true
  try {
    const res = await queryAssetPage(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载资产列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  queryParams.pageNum = 1
  loadAssetList()
}

/**
 * 重置
 */
const handleReset = () => {
  queryParams.pageNum = 1
  queryParams.assetName = ''
  queryParams.assetCode = ''
  queryParams.status = null
  queryParams.location = ''
  loadAssetList()
}

/**
 * 分页大小变化
 */
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  loadAssetList()
}

/**
 * 页码变化
 */
const handleCurrentChange = (page) => {
  queryParams.pageNum = page
  loadAssetList()
}

/**
 * 新增资产
 */
const handleAdd = () => {
  router.push('/asset/add')
}

/**
 * 查看详情
 */
const handleDetail = (row) => {
  router.push(`/asset/detail/${row.id}`)
}

/**
 * 编辑资产
 */
const handleEdit = (row) => {
  router.push(`/asset/edit/${row.id}`)
}

/**
 * 删除资产
 */
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除资产"${row.assetName}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteAsset(row.id)
      ElMessage.success('删除成功')
      loadAssetList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

/**
 * 查看二维码
 */
const handleViewQrCode = async (row) => {
  currentAsset.value = row
  qrCodeDialogVisible.value = true
  qrCodeBase64.value = ''
  
  try {
    const res = await generateQrCode(row.id)
    qrCodeBase64.value = res.data
  } catch (error) {
    console.error('获取二维码失败:', error)
    ElMessage.error('获取二维码失败')
  }
}

/**
 * 打印二维码
 */
const handlePrintQrCode = () => {
  // 创建打印窗口
  const printWindow = window.open('', '_blank')
  const html = '<!DOCTYPE html><html><head><title>打印二维码</title>' +
    '<style>body{font-family:Arial,sans-serif;text-align:center;padding:20px;}' +
    '.qrcode{margin:20px auto;}.qrcode img{width:200px;height:200px;}' +
    '.info{margin-top:20px;font-size:14px;}.info p{margin:5px 0;}</style></head>' +
    '<body><h2>资产二维码</h2><div class="qrcode">' +
    '<img src="' + qrCodeBase64.value + '" alt="二维码" /></div>' +
    '<div class="info"><p><strong>资产名称：</strong>' + currentAsset.value.assetName + '</p>' +
    '<p><strong>资产编码：</strong>' + currentAsset.value.assetCode + '</p>' +
    '<p><strong>UUID：</strong>' + currentAsset.value.uuid + '</p></div>' +
    '<scr' + 'ipt>window.onload=function(){window.print();window.close();}</scr' + 'ipt>' +
    '</body></html>'
  printWindow.document.write(html)
  printWindow.document.close()
}

/**
 * 导出Excel
 */
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 组件挂载
onMounted(() => {
  loadAssetList()
})
</script>

<style lang="scss" scoped>
.asset-list-container {
  .search-card {
    margin-bottom: 15px;
    
    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }
  
  .toolbar-card {
    margin-bottom: 15px;
  }
  
  .table-card {
    .el-pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }
}

.qrcode-container {
  text-align: center;
  
  .qrcode-info {
    text-align: left;
    padding: 10px;
    background-color: #f5f7fa;
    border-radius: 4px;
    margin-bottom: 20px;
    
    p {
      margin: 8px 0;
      font-size: 14px;
    }
  }
  
  .qrcode-image {
    img {
      width: 200px;
      height: 200px;
    }
  }
}
</style>
