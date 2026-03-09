/**
 * 数据统计相关 API
 * 
 * 接口列表：
 * - getDashboardData: 获取看板数据
 * - getStatusDistribution: 资产状态分布
 * - getDeptValueRanking: 部门资产价值排名
 * - getDepreciationTrend: 折旧趋势
 * 
 * @author 毕业设计项目组
 */
import request from '@/utils/request'

/**
 * 获取看板统计数据
 * 
 * @returns {Promise}
 */
export function getDashboardData() {
  return request({
    url: '/statistics/dashboard',
    method: 'get'
  })
}

/**
 * 获取资产状态分布
 * 
 * @returns {Promise}
 */
export function getStatusDistribution() {
  return request({
    url: '/statistics/status-distribution',
    method: 'get'
  })
}

/**
 * 获取部门资产价值排名
 * 
 * @returns {Promise}
 */
export function getDeptValueRanking() {
  return request({
    url: '/statistics/dept-value-ranking',
    method: 'get'
  })
}

/**
 * 获取近6个月折旧趋势
 * 
 * @returns {Promise}
 */
export function getDepreciationTrend() {
  return request({
    url: '/statistics/depreciation-trend',
    method: 'get'
  })
}
