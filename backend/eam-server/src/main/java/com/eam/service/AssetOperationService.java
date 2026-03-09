package com.eam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eam.dto.AssetOperationDTO;
import com.eam.dto.AssetQueryDTO;
import com.eam.entity.AstAsset;
import com.eam.entity.AstOperationRecord;

import java.util.List;

/**
 * 资产流转服务接口
 * 
 * 定义资产流转相关的业务方法：
 * - 领用：闲置资产被员工领用
 * - 退库：在用资产退回仓库
 * - 调拨：资产在部门间转移
 * - 维修：资产故障报修
 * - 报废：资产报废处理
 * 
 * @author 毕业设计项目组
 */
public interface AssetOperationService {

    /**
     * 资产领用
     * 
     * 业务规则：
     * - 只有闲置状态的资产才能被领用
     * - 需要指定目标部门和使用人
     * - 状态变更：闲置(0) -> 在用(1)
     * 
     * @param dto 操作请求
     */
    void apply(AssetOperationDTO dto);

    /**
     * 资产退库
     * 
     * 业务规则：
     * - 只有在用状态的资产才能退库
     * - 清空使用人和部门信息
     * - 状态变更：在用(1) -> 闲置(0)
     * 
     * @param dto 操作请求
     */
    void returnAsset(AssetOperationDTO dto);

    /**
     * 资产调拨
     * 
     * 业务规则：
     * - 只有在用状态的资产才能调拨
     * - 变更部门和使用人信息
     * - 状态保持：在用(1)
     * 
     * @param dto 操作请求
     */
    void transfer(AssetOperationDTO dto);

    /**
     * 资产报修
     * 
     * 业务规则：
     * - 只有在用状态的资产才能报修
     * - 状态变更：在用(1) -> 维修中(2)
     * 
     * @param dto 操作请求
     */
    void repair(AssetOperationDTO dto);

    /**
     * 资产报废
     * 
     * 业务规则：
     * - 任意状态的资产都可以报废
     * - 报废后停止折旧
     * - 状态变更：任意 -> 报废(3)
     * 
     * @param dto 操作请求
     */
    void scrap(AssetOperationDTO dto);

    /**
     * 维修完成
     * 
     * 业务规则：
     * - 只有维修中状态的资产才能完成维修
     * - 状态变更：维修中(2) -> 在用(1)
     * 
     * @param assetId 资产ID
     * @param description 维修结果描述
     */
    void completeRepair(Long assetId, String description);

    /**
     * 查询资产操作记录
     * 
     * @param assetId 资产ID
     * @return 操作记录列表
     */
    List<AstOperationRecord> getOperationRecords(Long assetId);

    /**
     * 分页查询操作记录
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param assetId 资产ID（可选）
     * @param operateType 操作类型（可选）
     * @return 分页结果
     */
    Page<AstOperationRecord> queryRecordPage(Integer pageNum, Integer pageSize, Long assetId, Integer operateType);

    /**
     * 查询闲置资产列表（闲置超过30天）
     * 
     * 用于闲置资产盘活公示
     * 
     * @param queryDTO 查询条件
     * @return 闲置资产列表
     */
    Page<AstAsset> queryIdleAssets(AssetQueryDTO queryDTO);

    /**
     * 申请调拨闲置资产
     * 
     * 业务规则：
     * - 只能申请闲置超过30天的资产
     * - 需要审批
     * 
     * @param dto 操作请求
     */
    void applyIdleAsset(AssetOperationDTO dto);
}
