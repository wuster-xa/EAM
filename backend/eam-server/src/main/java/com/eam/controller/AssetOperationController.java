package com.eam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eam.common.Result;
import com.eam.dto.AssetOperationDTO;
import com.eam.dto.AssetQueryDTO;
import com.eam.entity.AstAsset;
import com.eam.entity.AstOperationRecord;
import com.eam.service.AssetOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资产流转控制器
 * 
 * 提供资产流转操作的 RESTful 接口：
 * - 领用：POST /operation/apply
 * - 退库：POST /operation/return
 * - 调拨：POST /operation/transfer
 * - 报修：POST /operation/repair
 * - 报废：POST /operation/scrap
 * - 维修完成：POST /operation/complete-repair
 * - 操作记录：GET /operation/records/{assetId}
 * - 闲置资产：GET /operation/idle
 * 
 * @author 毕业设计项目组
 */
@Tag(name = "资产流转", description = "资产领用、退库、调拨、维修、报废等操作接口")
@RestController
@RequestMapping("/operation")
@RequiredArgsConstructor
public class AssetOperationController {

    private final AssetOperationService operationService;

    /**
     * 资产领用
     * 
     * 业务规则：
     * - 只有闲置状态的资产才能被领用
     * - 需要指定目标部门和使用人
     * 
     * @param dto 操作请求
     * @return 成功响应
     */
    @Operation(summary = "资产领用", description = "闲置资产被员工领用，状态变更为在用")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ASSET_MANAGER', 'EMPLOYEE')")
    @PostMapping("/apply")
    public Result<Void> apply(@Valid @RequestBody AssetOperationDTO dto) {
        operationService.apply(dto);
        return Result.success("领用成功", null);
    }

    /**
     * 资产退库
     * 
     * 业务规则：
     * - 只有在用状态的资产才能退库
     * 
     * @param dto 操作请求
     * @return 成功响应
     */
    @Operation(summary = "资产退库", description = "在用资产退回仓库，状态变更为闲置")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ASSET_MANAGER', 'DEPT_MANAGER')")
    @PostMapping("/return")
    public Result<Void> returnAsset(@Valid @RequestBody AssetOperationDTO dto) {
        operationService.returnAsset(dto);
        return Result.success("退库成功", null);
    }

    /**
     * 资产调拨
     * 
     * 业务规则：
     * - 只有在用状态的资产才能调拨
     * - 支持跨部门调拨
     * 
     * @param dto 操作请求
     * @return 成功响应
     */
    @Operation(summary = "资产调拨", description = "资产在部门间转移")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ASSET_MANAGER', 'DEPT_MANAGER')")
    @PostMapping("/transfer")
    public Result<Void> transfer(@Valid @RequestBody AssetOperationDTO dto) {
        operationService.transfer(dto);
        return Result.success("调拨成功", null);
    }

    /**
     * 资产报修
     * 
     * 业务规则：
     * - 只有在用状态的资产才能报修
     * 
     * @param dto 操作请求
     * @return 成功响应
     */
    @Operation(summary = "资产报修", description = "在用资产故障报修，状态变更为维修中")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ASSET_MANAGER', 'EMPLOYEE')")
    @PostMapping("/repair")
    public Result<Void> repair(@Valid @RequestBody AssetOperationDTO dto) {
        operationService.repair(dto);
        return Result.success("报修成功", null);
    }

    /**
     * 资产报废
     * 
     * 业务规则：
     * - 任意状态的资产都可以报废（报废资产除外）
     * - 报废后停止折旧
     * 
     * @param dto 操作请求
     * @return 成功响应
     */
    @Operation(summary = "资产报废", description = "资产报废处理，停止折旧")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ASSET_MANAGER')")
    @PostMapping("/scrap")
    public Result<Void> scrap(@Valid @RequestBody AssetOperationDTO dto) {
        operationService.scrap(dto);
        return Result.success("报废成功", null);
    }

    /**
     * 维修完成
     * 
     * @param assetId 资产ID
     * @param description 维修结果描述
     * @return 成功响应
     */
    @Operation(summary = "维修完成", description = "维修完成，状态变更为在用")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ASSET_MANAGER')")
    @PostMapping("/complete-repair")
    public Result<Void> completeRepair(
            @Parameter(description = "资产ID") @RequestParam Long assetId,
            @Parameter(description = "维修结果描述") @RequestParam(required = false) String description) {
        operationService.completeRepair(assetId, description);
        return Result.success("维修完成", null);
    }

    /**
     * 查询资产操作记录
     * 
     * @param assetId 资产ID
     * @return 操作记录列表
     */
    @Operation(summary = "查询操作记录", description = "查询指定资产的所有操作记录")
    @GetMapping("/records/{assetId}")
    public Result<List<AstOperationRecord>> getRecords(
            @Parameter(description = "资产ID") @PathVariable Long assetId) {
        List<AstOperationRecord> records = operationService.getOperationRecords(assetId);
        return Result.success(records);
    }

    /**
     * 分页查询操作记录
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param assetId 资产ID（可选）
     * @param operateType 操作类型（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询操作记录", description = "分页查询资产操作记录")
    @GetMapping("/records/page")
    public Result<Page<AstOperationRecord>> queryRecordPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "资产ID") @RequestParam(required = false) Long assetId,
            @Parameter(description = "操作类型") @RequestParam(required = false) Integer operateType) {
        Page<AstOperationRecord> page = operationService.queryRecordPage(pageNum, pageSize, assetId, operateType);
        return Result.success(page);
    }

    /**
     * 查询闲置资产列表（闲置超过30天）
     * 
     * 用于闲置资产盘活公示
     * 
     * @param queryDTO 查询条件
     * @return 闲置资产列表
     */
    @Operation(summary = "查询闲置资产", description = "查询闲置超过30天的资产，用于闲置资产盘活公示")
    @GetMapping("/idle")
    public Result<Page<AstAsset>> queryIdleAssets(AssetQueryDTO queryDTO) {
        Page<AstAsset> page = operationService.queryIdleAssets(queryDTO);
        return Result.success(page);
    }

    /**
     * 申请调拨闲置资产
     * 
     * 业务规则：
     * - 只能申请闲置超过30天的资产
     * 
     * @param dto 操作请求
     * @return 成功响应
     */
    @Operation(summary = "申请闲置资产", description = "申请调拨闲置超过30天的资产")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ASSET_MANAGER', 'DEPT_MANAGER', 'EMPLOYEE')")
    @PostMapping("/idle/apply")
    public Result<Void> applyIdleAsset(@Valid @RequestBody AssetOperationDTO dto) {
        operationService.applyIdleAsset(dto);
        return Result.success("申请成功", null);
    }
}
