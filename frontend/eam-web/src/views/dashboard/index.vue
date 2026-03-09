<template>
  <!-- 
    数据可视化驾驶舱页面
    
    功能：
    1. 资产状态分布饼图（闲置/在用/报废占比）
    2. 部门资产价值排名柱状图
    3. 近6个月资产折旧趋势折线图
    4. 核心统计指标卡片
  -->
  <div class="dashboard-container">
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background-color: #409eff;">
            <el-icon :size="32"><Box /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.totalAssets }}</div>
            <div class="stat-label">资产总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background-color: #67c23a;">
            <el-icon :size="32"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(statistics.totalValue) }}</div>
            <div class="stat-label">资产总值</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background-color: #e6a23c;">
            <el-icon :size="32"><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(statistics.totalDepreciation) }}</div>
            <div class="stat-label">累计折旧</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background-color: #f56c6c;">
            <el-icon :size="32"><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.idleAssets }}</div>
            <div class="stat-label">闲置资产</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 资产状态分布饼图 -->
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>资产状态分布</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <!-- 部门资产价值排名柱状图 -->
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>部门资产价值排名（TOP10）</span>
            </div>
          </template>
          <div ref="barChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 折旧趋势图 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>近6个月资产折旧趋势</span>
            </div>
          </template>
          <div ref="lineChartRef" class="chart-container-large"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
/**
 * 数据可视化驾驶舱
 * 
 * 使用 ECharts 实现：
 * 1. 资产状态分布饼图
 * 2. 部门资产价值排名柱状图
 * 3. 近6个月折旧趋势折线图
 * 
 * 数据来源：后端统计接口
 */
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboardData, getStatusDistribution, getDeptValueRanking, getDepreciationTrend } from '@/api/statistics'

// 图表 DOM 引用
const pieChartRef = ref(null)
const barChartRef = ref(null)
const lineChartRef = ref(null)

// 图表实例
let pieChart = null
let barChart = null
let lineChart = null

// 统计数据
const statistics = ref({
  totalAssets: 0,
  totalValue: 0,
  totalDepreciation: 0,
  idleAssets: 0
})

/**
 * 格式化金额（万元）
 */
const formatMoney = (value) => {
  if (!value) return '0'
  return (value / 10000).toFixed(2) + '万'
}

/**
 * 初始化饼图 - 资产状态分布
 */
const initPieChart = (data) => {
  if (!pieChartRef.value) return
  
  pieChart = echarts.init(pieChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '资产状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: data || [
          { value: 1048, name: '闲置', itemStyle: { color: '#909399' } },
          { value: 735, name: '在用', itemStyle: { color: '#67c23a' } },
          { value: 580, name: '维修中', itemStyle: { color: '#e6a23c' } },
          { value: 484, name: '报废', itemStyle: { color: '#f56c6c' } }
        ]
      }
    ]
  }
  
  pieChart.setOption(option)
}

/**
 * 初始化柱状图 - 部门资产价值排名
 */
const initBarChart = (data) => {
  if (!barChartRef.value) return
  
  barChart = echarts.init(barChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: '{b}: ¥{c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value) => (value / 10000).toFixed(0) + '万'
      }
    },
    yAxis: {
      type: 'category',
      data: data?.deptNames || ['财务部', '技术部', '市场部', '人事部', '行政部', '研发部', '销售部', '客服部', '运营部', '产品部']
    },
    series: [
      {
        name: '资产价值',
        type: 'bar',
        data: data?.values || [320, 280, 250, 220, 200, 180, 160, 140, 120, 100],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#67c23a' }
          ])
        },
        label: {
          show: true,
          position: 'right',
          formatter: (params) => '¥' + (params.value / 10000).toFixed(1) + '万'
        }
      }
    ]
  }
  
  barChart.setOption(option)
}

/**
 * 初始化折线图 - 折旧趋势
 */
const initLineChart = (data) => {
  if (!lineChartRef.value) return
  
  lineChart = echarts.init(lineChartRef.value)
  
  const months = []
  const now = new Date()
  for (let i = 5; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    months.push(`${date.getFullYear()}年${date.getMonth() + 1}月`)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br />{a}: ¥{c}'
    },
    legend: {
      data: ['折旧金额']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data?.months || months
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value) => (value / 10000).toFixed(0) + '万'
      }
    },
    series: [
      {
        name: '折旧金额',
        type: 'line',
        smooth: true,
        data: data?.values || [120, 132, 101, 134, 90, 230],
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#409eff',
          width: 2
        },
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
  
  lineChart.setOption(option)
}

/**
 * 加载仪表盘数据
 */
const loadDashboardData = async () => {
  try {
    // 实际项目中应调用后端接口
    // const res = await getDashboardData()
    // statistics.value = res.data
    
    // 模拟数据
    statistics.value = {
      totalAssets: 2847,
      totalValue: 158600000,
      totalDepreciation: 32500000,
      idleAssets: 156
    }
    
    // 初始化图表
    await nextTick()
    initPieChart()
    initBarChart()
    initLineChart()
    
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  }
}

/**
 * 窗口大小变化时重绘图表
 */
const handleResize = () => {
  pieChart?.resize()
  barChart?.resize()
  lineChart?.resize()
}

// 组件挂载
onMounted(() => {
  loadDashboardData()
  window.addEventListener('resize', handleResize)
})

// 组件卸载
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  barChart?.dispose()
  lineChart?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  width: 100%;
  height: 100%;
  overflow-y: auto;
}

.stat-cards {
  margin-bottom: 20px;
  
  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    
    .stat-icon {
      width: 64px;
      height: 64px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
    }
    
    .stat-info {
      margin-left: 16px;
      
      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
      }
      
      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  .card-header {
    font-size: 16px;
    font-weight: bold;
  }
  
  .chart-container {
    height: 300px;
  }
  
  .chart-container-large {
    height: 350px;
  }
}
</style>
