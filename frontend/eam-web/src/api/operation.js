/**
 * 资产流转操作相关 API
 * 
 * 接口列表：
 * - applyAsset: 资产领用
 * - returnAsset: 资产退库
 * - transferAsset: 资产调拨
 * - repairAsset: 资产报修
 * - scrapAsset: 资产报废
 * - completeRepair: 维修完成
 * - getOperationRecords: 查询操作记录
 * - queryRecordPage: 分页查询操作记录
 * - queryIdleAssets: 查询闲置资产
 * - applyIdleAsset: 申请闲置资产
 * 
 * @author 毕业设计项目组
 */
import request from '@/utils/request'

/**
 * 资产领用
 * 
 * @param {Object} data - 操作参数
 * @returns {Promise}
 */
export function applyAsset(data) {
  return request({
    url: '/operation/apply',
    method: 'post',
    data
  })
}

/**
 * 资产退库
 * 
 * @param {Object} data - 操作参数
 * @returns {Promise}
 */
export function returnAsset(data) {
  return request({
    url: '/operation/return',
    method: 'post',
    data
  })
}

/**
 * 资产调拨
 * 
 * @param {Object} data - 操作参数
 * @returns {Promise}
 */
export function transferAsset(data) {
  return request({
    url: '/operation/transfer',
    method: 'post',
    data
  })
}

/**
 * 资产报修
 * 
 * @param {Object} data - 操作参数
 * @returns {Promise}
 */
export function repairAsset(data) {
  return request({
    url: '/operation/repair',
    method: 'post',
    data
  })
}

/**
 * 资产报废
 * 
 * @param {Object} data - 操作参数
 * @returns {Promise}
 */
export function scrapAsset(data) {
  return request({
    url: '/operation/scrap',
    method: 'post',
    data
  })
}

/**
 * 维修完成
 * 
 * @param {number} assetId - 资产ID
 * @param {string} description - 维修结果描述
 * @returns {Promise}
 */
export function completeRepair(assetId, description) {
  return request({
    url: '/operation/complete-repair',
    method: 'post',
    params: { assetId, description }
  })
}

/**
 * 查询资产操作记录
 * 
 * @param {number} assetId - 资产ID
 * @returns {Promise}
 */
export function getOperationRecords(assetId) {
  return request({
    url: `/operation/records/${assetId}`,
    method: 'get'
  })
}

/**
 * 分页查询操作记录
 * 
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function queryRecordPage(params) {
  return request({
    url: '/operation/records/page',
    method: 'get',
    params
  })
}

/**
 * 查询闲置资产列表（闲置超过30天）
 * 
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function queryIdleAssets(params) {
  return request({
    url: '/operation/idle',
    method: 'get',
    params
  })
}

/**
 * 申请闲置资产
 * 
 * @param {Object} data - 操作参数
 * @returns {Promise}
 */
export function applyIdleAsset(data) {
  return request({
    url: '/operation/idle/apply',
    method: 'post',
    data
  })
}
