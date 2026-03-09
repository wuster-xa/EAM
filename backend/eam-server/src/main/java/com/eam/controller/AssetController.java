package com.eam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eam.common.Result;
import com.eam.dto.AssetDTO;
import com.eam.dto.AssetQueryDTO;
import com.eam.entity.AstAsset;
import com.eam.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 资产管理控制器
 * 
 * 提供资产管理相关的 RESTful 接口
 * 
 * 接口列表：
 * - GET  /asset/page: 分页查询资产列表
 * - GET  /asset/{id}: 查询资产详情
 * - POST /asset: 新增资产
 * - PUT  /asset: 修改资产
 * - DELETE /asset/{id}: 删除资产
 * - GET  /asset/qrcode/{id}: 生成二维码
 * - PUT  /asset/status: 更新资产状态
 * 
 * 权限说明：
 * - 查询：所有登录用户
 * - 新增/修改/删除：资产管理员及以上
 * 
 * @author 毕业设计项目组
 */
@Tag(name = "资产管理", description = "资产信息管理相关接口")
@RestController
@RequestMapping("/asset")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

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
    @Operation(summary = "分页查询资产列表", description = "支持多条件筛选，返回分页数据")
    @GetMapping("/page")
    public Result<Page<AstAsset>> queryPage(AssetQueryDTO queryDTO) {
        Page<AstAsset> page = assetService.queryAssetPage(queryDTO);
        return Result.success(page);
    }

    /**
     * 查询资产详情
     * 
     * 返回资产完整信息，包含二维码
     * 
     * @param id 资产ID
     * @return 资产详情
     */
    @Operation(summary = "查询资产详情", description = "根据ID查询资产详情，包含二维码")
    @GetMapping("/{id}")
    public Result<AstAsset> getById(
            @Parameter(description = "资产ID") @PathVariable Long id) {
        AstAsset asset = assetService.getAssetById(id);
        return Result.success(asset);
    }

    /**
     * 新增资产
     * 
     * 业务流程：
     * 1. 自动生成 UUID
     * 2. 初始化价值字段
     * 3. 设置初始状态为闲置
     * 4. 生成二维码
     * 
     * 权限要求：资产管理员及以上
     * 
     * @param assetDTO 资产信息
     * @return 新增后的资产
     */
    @Operation(summary = "新增资产", description = "入库新资产，自动生成UUID和二维码")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ASSET_MANAGER')")
    @PostMapping
    public Result<AstAsset> add(@Valid @RequestBody AssetDTO assetDTO) {
        AstAsset asset = assetService.addAsset(assetDTO);
        return Result.success("资产入库成功", asset);
    }

    /**
     * 修改资产信息
     * 
     * 权限要求：资产管理员及以上
     * 
     * @param assetDTO 资产信息
     * @return 修改后的资产
     */
    @Operation(summary = "修改资产", description = "修改资产信息，使用乐观锁防止并发覆盖")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ASSET_MANAGER')")
    @PutMapping
    public Result<AstAsset> update(@Valid @RequestBody AssetDTO assetDTO) {
        AstAsset asset = assetService.updateAsset(assetDTO);
        return Result.success("修改成功", asset);
    }

    /**
     * 删除资产（逻辑删除）
     * 
     * 注意：只有报废状态的资产才能删除
     * 
     * 权限要求：资产管理员及以上
     * 
     * @param id 资产ID
     * @return 成功响应
     */
    @Operation(summary = "删除资产", description = "逻辑删除，只有报废状态的资产才能删除")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ASSET_MANAGER')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "资产ID") @PathVariable Long id) {
        assetService.deleteAsset(id);
        return Result.success("删除成功", null);
    }

    /**
     * 生成资产二维码
     * 
     * 根据资产 UUID 生成二维码 Base64 字符串
     * 
     * @param id 资产ID
     * @return 二维码 Base64 字符串
     */
    @Operation(summary = "生成二维码", description = "根据资产UUID生成二维码Base64字符串")
    @GetMapping("/qrcode/{id}")
    public Result<String> generateQrCode(
            @Parameter(description = "资产ID") @PathVariable Long id) {
        String qrCode = assetService.generateQrCode(id);
        return Result.success(qrCode);
    }

    /**
     * 更新资产状态
     * 
     * 状态流转规则：
     * - 闲置(0) -> 在用(1)：领用
     * - 在用(1) -> 闲置(0)：退库
     * - 在用(1) -> 维修中(2)：报修
     * - 维修中(2) -> 在用(1)：维修完成
     * - 任意 -> 报废(3)：报废
     * 
     * 权限要求：资产管理员及以上
     * 
     * @param id 资产ID
     * @param status 新状态
     * @return 成功响应
     */
    @Operation(summary = "更新资产状态", description = "更新资产状态，校验状态流转合法性")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ASSET_MANAGER')")
    @PutMapping("/status")
    public Result<Void> updateStatus(
            @Parameter(description = "资产ID") @RequestParam Long id,
            @Parameter(description = "新状态：0-闲置，1-在用，2-维修中，3-报废") @RequestParam Integer status) {
        assetService.updateStatus(id, status);
        return Result.success("状态更新成功", null);
    }
}
