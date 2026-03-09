<template>
  <!-- 
    资产领用申请页面
    
    功能：
    1. 选择闲置资产
    2. 填写领用信息
    3. 提交领用申请
  -->
  <div class="apply-container">
    <el-card shadow="never">
      <template #header>
        <span>资产领用申请</span>
      </template>
      
      <el-form
        ref="formRef"
        :model="applyForm"
        :rules="applyRules"
        label-width="100px"
        style="max-width: 600px;"
      >
        <el-form-item label="选择资产" prop="assetId">
          <el-select
            v-model="applyForm.assetId"
            filterable
            placeholder="请选择闲置资产"
            style="width: 100%;"
            @change="handleAssetChange"
          >
            <el-option
              v-for="asset in idleAssetList"
              :key="asset.id"
              :label="`${asset.assetName} (${asset.assetCode})`"
              :value="asset.id"
            >
              <div style="display: flex; justify-content: space-between;">
                <span>{{ asset.assetName }}</span>
                <span style="color: #909399; font-size: 12px;">{{ asset.assetCode }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="资产信息" v-if="selectedAsset">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="资产名称">{{ selectedAsset.assetName }}</el-descriptions-item>
            <el-descriptions-item label="资产编码">{{ selectedAsset.assetCode }}</el-descriptions-item>
            <el-descriptions-item label="品牌型号">{{ selectedAsset.brand }} {{ selectedAsset.model }}</el-descriptions-item>
            <el-descriptions-item label="存放位置">{{ selectedAsset.location }}</el-descriptions-item>
            <el-descriptions-item label="资产原值">¥{{ formatMoney(selectedAsset.originalValue) }}</el-descriptions-item>
            <el-descriptions-item label="当前净值">¥{{ formatMoney(selectedAsset.netValue) }}</el-descriptions-item>
          </el-descriptions>
        </el-form-item>
        
        <el-form-item label="领用部门" prop="toDeptId">
          <el-select v-model="applyForm.toDeptId" placeholder="请选择领用部门" style="width: 100%;">
            <el-option label="财务部" :value="1" />
            <el-option label="技术部" :value="2" />
            <el-option label="市场部" :value="3" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="领用人" prop="toUserId">
          <el-select v-model="applyForm.toUserId" placeholder="请选择领用人" style="width: 100%;">
            <el-option label="张三" :value="1" />
            <el-option label="李四" :value="2" />
            <el-option label="王五" :value="3" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="申请理由" prop="description">
          <el-input
            v-model="applyForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入申请理由"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            提交申请
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 资产领用申请页面
 * 
 * 功能：
 * 1. 从闲置资产列表中选择
 * 2. 填写领用部门和领用人
 * 3. 提交领用申请
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { queryIdleAssets, applyAsset } from '@/api/operation'

const router = useRouter()

// 表单引用
const formRef = ref(null)

// 加载状态
const loading = ref(false)

// 闲置资产列表
const idleAssetList = ref([])

// 选中的资产
const selectedAsset = computed(() => {
  return idleAssetList.value.find(a => a.id === applyForm.assetId)
})

// 申请表单
const applyForm = reactive({
  assetId: null,
  operateType: 2,  // 领用
  toDeptId: null,
  toUserId: null,
  description: ''
})

// 表单验证规则
const applyRules = {
  assetId: [
    { required: true, message: '请选择资产', trigger: 'change' }
  ],
  toDeptId: [
    { required: true, message: '请选择领用部门', trigger: 'change' }
  ],
  toUserId: [
    { required: true, message: '请选择领用人', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入申请理由', trigger: 'blur' }
  ]
}

/**
 * 格式化金额
 */
const formatMoney = (value) => {
  if (!value) return '0.00'
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

/**
 * 资产选择变化
 */
const handleAssetChange = (assetId) => {
  // 资产选择后的回调
}

/**
 * 加载闲置资产列表
 */
const loadIdleAssets = async () => {
  try {
    const res = await queryIdleAssets({ pageNum: 1, pageSize: 100 })
    idleAssetList.value = res.data.records
  } catch (error) {
    console.error('加载闲置资产失败:', error)
  }
}

/**
 * 提交申请
 */
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  
  try {
    await applyAsset(applyForm)
    ElMessage.success('领用申请提交成功')
    router.push('/operation/records')
  } catch (error) {
    console.error('提交申请失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 重置表单
 */
const handleReset = () => {
  formRef.value.resetFields()
}

// 组件挂载
onMounted(() => {
  loadIdleAssets()
})
</script>

<style lang="scss" scoped>
.apply-container {
  max-width: 800px;
  margin: 0 auto;
}
</style>
