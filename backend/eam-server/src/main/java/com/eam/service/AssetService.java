package com.eam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eam.dto.AssetDTO;
import com.eam.dto.AssetQueryDTO;
import com.eam.entity.AstAsset;

/**
 * 资产服务接口
 * 
 * 定义资产管理相关的业务方法
 * 
 * @author 毕业设计项目组
 */
public interface AssetService {

    /**
     * 分页查询资产列表
     * 
     * 支持多条件筛选：
     * - 资产名称、编码模糊查询
     * - 分类、状态、部门精确筛选
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<AstAsset> queryAssetPage(AssetQueryDTO queryDTO);

    /**
     * 根据ID查询资产详情
     * 
     * 包含：
     * - 资产基本信息
     * - 二维码 Base64 字符串
     * - 分类名称、部门名称、使用人姓名
     * 
     * @param id 资产ID
     * @return 资产详情
     */
    AstAsset getAssetById(Long id);

    /**
     * 新增资产
     * 
     * 业务流程：
     * 1. 自动生成 UUID
     * 2. 初始化净值 = 原值
     * 3. 初始化累计折旧 = 0
     * 4. 设置初始状态 = 闲置
     * 5. 生成二维码
     * 
     * @param assetDTO 资产信息
     * @return 新增后的资产
     */
    AstAsset addAsset(AssetDTO assetDTO);

    /**
     * 修改资产信息
     * 
     * 注意：
     * - 原值修改后需重新计算净值
     * - 使用乐观锁防止并发覆盖
     * 
     * @param assetDTO 资产信息
     * @return 修改后的资产
     */
    AstAsset updateAsset(AssetDTO assetDTO);

    /**
     * 删除资产（逻辑删除）
     * 
     * @param id 资产ID
     */
    void deleteAsset(Long id);

    /**
     * 生成资产二维码
     * 
     * 根据资产 UUID 生成二维码 Base64 字符串
     * 
     * @param id 资产ID
     * @return 二维码 Base64 字符串
     */
    String generateQrCode(Long id);

    /**
     * 更新资产状态
     * 
     * 状态流转：
     * - 闲置 -> 在用（领用）
     * - 在用 -> 闲置（退库）
     * - 在用 -> 维修中（报修）
     * - 维修中 -> 在用（维修完成）
     * - 任意 -> 报废（报废）
     * 
     * @param id 资产ID
     * @param status 新状态
     */
    void updateStatus(Long id, Integer status);
}
