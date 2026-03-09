<template>
  <!-- 
    审批管理页面
    
    功能：
    1. 展示待审批的资产领用申请列表
    2. 管理员可点击"通过/驳回"进行审批
    3. 清晰展示审批状态
  -->
  <div class="approval-container">
    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="待审批" name="pending">
        <template #label>
          <span>
            <el-icon><Clock /></el-icon>
            待审批
            <el-badge :value="pendingCount" class="tab-badge" />
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已通过" name="approved">
        <template #label>
          <span>
            <el-icon><CircleCheck /></el-icon>
            已通过
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已驳回" name="rejected">
        <template #label>
          <span>
            <el-icon><CircleClose /></el-icon>
            已驳回
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 申请列表 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="assetName" label="资产名称" min-width="150" />
        <el-table-column prop="assetCode" label="资产编码" width="120" />
        <el-table-column prop="operateType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getOperateTypeName(row.operateType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fromDeptName" label="原部门" width="120" />
        <el-table-column prop="toDeptName" label="目标部门" width="120" />
        <el-table-column prop="toUserName" label="申请人" width="100" />
        <el-table-column prop="description" label="申请理由" min-width="200" show-overflow-tooltip />
        <el-table-column prop="operateTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.operateTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="approvalStatus" label="审批状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.approvalStatus)">
              {{ getApprovalStatusName(row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.approvalStatus === 1">
              <el-button type="success" @click="handleApprove(row, true)">
                <el-icon><Select /></el-icon>
                通过
              </el-button>
              <el-button type="danger" @click="handleApprove(row, false)">
                <el-icon><CloseBold /></el-icon>
                驳回
              </el-button>
            </template>
            <template v-else>
              <el-button type="primary" link @click="handleViewDetail(row)">
                查看详情
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadApprovalList"
        @current-change="loadApprovalList"
      />
    </el-card>
    
    <!-- 审批弹窗 -->
    <el-dialog
      v-model="approvalDialogVisible"
      :title="isApprove ? '审批通过' : '审批驳回'"
      width="500px"
    >
      <el-form :model="approvalForm" label-width="80px">
        <el-form-item label="审批意见">
          <el-input
            v-model="approvalForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button :type="isApprove ? 'success' : 'danger'" @click="submitApproval">
          确认{{ isApprove ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 审批管理页面
 * 
 * 功能：
 * 1. 展示待审批/已通过/已驳回的申请列表
 * 2. 管理员可进行审批操作
 * 3. 清晰展示审批状态
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { queryRecordPage } from '@/api/operation'

// 当前标签
const activeTab = ref('pending')

// 加载状态
const loading = ref(false)

// 待审批数量
const pendingCount = ref(0)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  approvalStatus: 1  // 默认查询待审批
})

// 表格数据
const tableData = ref([])
const total = ref(0)

// 审批弹窗
const approvalDialogVisible = ref(false)
const isApprove = ref(true)
const currentRecord = ref(null)
const approvalForm = reactive({
  remark: ''
})

/**
 * 获取操作类型名称
 */
const getOperateTypeName = (type) => {
  const typeMap = {
    1: '入库',
    2: '领用',
    3: '退库',
    4: '调拨',
    5: '维修',
    6: '报废'
  }
  return typeMap[type] || '未知'
}

/**
 * 获取审批状态名称
 */
const getApprovalStatusName = (status) => {
  const statusMap = {
    0: '无需审批',
    1: '待审批',
    2: '已通过',
    3: '已驳回'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取审批状态标签类型
 */
const getApprovalStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return typeMap[status] || 'info'
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

/**
 * 标签页切换
 */
const handleTabChange = (tab) => {
  const statusMap = {
    pending: 1,
    approved: 2,
    rejected: 3
  }
  queryParams.approvalStatus = statusMap[tab]
  queryParams.pageNum = 1
  loadApprovalList()
}

/**
 * 加载审批列表
 */
const loadApprovalList = async () => {
  loading.value = true
  try {
    const res = await queryRecordPage(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
    
    // 如果是待审批标签，更新待审批数量
    if (activeTab.value === 'pending') {
      pendingCount.value = res.data.total
    }
  } catch (error) {
    console.error('加载审批列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 处理审批
 */
const handleApprove = (row, approve) => {
  currentRecord.value = row
  isApprove.value = approve
  approvalForm.remark = ''
  approvalDialogVisible.value = true
}

/**
 * 提交审批
 */
const submitApproval = async () => {
  try {
    // TODO: 调用审批接口
    // await approveRecord({
    //   recordId: currentRecord.value.id,
    //   approved: isApprove.value,
    //   remark: approvalForm.remark
    // })
    
    ElMessage.success(isApprove.value ? '审批通过' : '审批驳回')
    approvalDialogVisible.value = false
    loadApprovalList()
  } catch (error) {
    console.error('审批失败:', error)
  }
}

/**
 * 查看详情
 */
const handleViewDetail = (row) => {
  ElMessage.info('详情功能开发中...')
}

// 组件挂载
onMounted(() => {
  loadApprovalList()
})
</script>

<style lang="scss" scoped>
.approval-container {
  .tab-badge {
    margin-left: 5px;
  }
  
  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>
