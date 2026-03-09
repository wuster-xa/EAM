/**
 * 资产管理相关 API
 * 
 * 接口列表：
 * - queryAssetPage: 分页查询资产列表
 * - getAssetById: 查询资产详情
 * - addAsset: 新增资产
 * - updateAsset: 修改资产
 * - deleteAsset: 删除资产
 * - generateQrCode: 生成二维码
 * - updateStatus: 更新资产状态
 * 
 * @author 毕业设计项目组
 */
import request from '@/utils/request'

/**
 * 分页查询资产列表
 * 
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function queryAssetPage(params) {
  return request({
    url: '/asset/page',
    method: 'get',
    params
  })
}

/**
 * 查询资产详情
 * 
 * @param {number} id - 资产ID
 * @returns {Promise}
 */
export function getAssetById(id) {
  return request({
    url: `/asset/${id}`,
    method: 'get'
  })
}

/**
 * 新增资产
 * 
 * @param {Object} data - 资产信息
 * @returns {Promise}
 */
export function addAsset(data) {
  return request({
    url: '/asset',
    method: 'post',
    data
  })
}

/**
 * 修改资产
 * 
 * @param {Object} data - 资产信息
 * @returns {Promise}
 */
export function updateAsset(data) {
  return request({
    url: '/asset',
    method: 'put',
    data
  })
}

/**
 * 删除资产
 * 
 * @param {number} id - 资产ID
 * @returns {Promise}
 */
export function deleteAsset(id) {
  return request({
    url: `/asset/${id}`,
    method: 'delete'
  })
}

/**
 * 生成二维码
 * 
 * @param {number} id - 资产ID
 * @returns {Promise}
 */
export function generateQrCode(id) {
  return request({
    url: `/asset/qrcode/${id}`,
    method: 'get'
  })
}

/**
 * 更新资产状态
 * 
 * @param {number} id - 资产ID
 * @param {number} status - 新状态
 * @returns {Promise}
 */
export function updateStatus(id, status) {
  return request({
    url: '/asset/status',
    method: 'put',
    params: { id, status }
  })
}

/**
 * 查询资产分类列表
 * 
 * @returns {Promise}
 */
export function getCategoryList() {
  return request({
    url: '/category/list',
    method: 'get'
  })
}

/**
 * 查询部门列表
 * 
 * @returns {Promise}
 */
export function getDeptList() {
  return request({
    url: '/dept/list',
    method: 'get'
  })
}
