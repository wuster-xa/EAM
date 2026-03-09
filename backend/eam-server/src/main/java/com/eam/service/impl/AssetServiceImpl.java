package com.eam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eam.dto.AssetDTO;
import com.eam.dto.AssetQueryDTO;
import com.eam.entity.AstAsset;
import com.eam.entity.AstCategory;
import com.eam.exception.BusinessException;
import com.eam.mapper.AstAssetMapper;
import com.eam.mapper.AstCategoryMapper;
import com.eam.service.AssetService;
import com.eam.util.QrCodeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 资产服务实现类
 * 
 * 实现资产管理的核心业务逻辑
 * 
 * 核心功能：
 * 1. 资产CRUD操作
 * 2. UUID自动生成
 * 3. 二维码生成
 * 4. 状态流转管理
 * 5. 乐观锁并发控制
 * 
 * @author 毕业设计项目组
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AstAssetMapper assetMapper;
    private final AstCategoryMapper categoryMapper;

    /**
     * 分页查询资产列表
     * 
     * 实现步骤：
     * 1. 构建查询条件
     * 2. 执行分页查询
     * 3. 填充关联信息（分类名称、部门名称等）
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public Page<AstAsset> queryAssetPage(AssetQueryDTO queryDTO) {
        // 构建分页对象
        Page<AstAsset> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 构建查询条件
        LambdaQueryWrapper<AstAsset> queryWrapper = new LambdaQueryWrapper<>();
        
        // 资产名称模糊查询
        if (StringUtils.hasText(queryDTO.getAssetName())) {
            queryWrapper.like(AstAsset::getAssetName, queryDTO.getAssetName());
        }
        
        // 资产编码模糊查询
        if (StringUtils.hasText(queryDTO.getAssetCode())) {
            queryWrapper.like(AstAsset::getAssetCode, queryDTO.getAssetCode());
        }
        
        // 分类精确查询
        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq(AstAsset::getCategoryId, queryDTO.getCategoryId());
        }
        
        // 状态精确查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(AstAsset::getStatus, queryDTO.getStatus());
        }
        
        // 部门精确查询
        if (queryDTO.getDeptId() != null) {
            queryWrapper.eq(AstAsset::getDeptId, queryDTO.getDeptId());
        }
        
        // 使用人精确查询
        if (queryDTO.getUserId() != null) {
            queryWrapper.eq(AstAsset::getUserId, queryDTO.getUserId());
        }
        
        // 品牌模糊查询
        if (StringUtils.hasText(queryDTO.getBrand())) {
            queryWrapper.like(AstAsset::getBrand, queryDTO.getBrand());
        }
        
        // 存放位置模糊查询
        if (StringUtils.hasText(queryDTO.getLocation())) {
            queryWrapper.like(AstAsset::getLocation, queryDTO.getLocation());
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(AstAsset::getCreateTime);
        
        // 执行分页查询
        Page<AstAsset> result = assetMapper.selectPage(page, queryWrapper);
        
        // 填充关联信息
        result.getRecords().forEach(this::fillRelatedInfo);
        
        return result;
    }

    /**
     * 根据ID查询资产详情
     * 
     * @param id 资产ID
     * @return 资产详情
     */
    @Override
    public AstAsset getAssetById(Long id) {
        AstAsset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }
        
        // 填充关联信息
        fillRelatedInfo(asset);
        
        // 生成二维码 Base64
        asset.setQrCodeBase64(QrCodeUtils.generateQrCodeBase64(asset.getUuid()));
        
        return asset;
    }

    /**
     * 新增资产
     * 
     * 业务流程：
     * 1. 生成 UUID
     * 2. 初始化价值字段
     * 3. 设置初始状态
     * 4. 设置折旧开始日期
     * 5. 保存到数据库
     * 
     * @param assetDTO 资产信息
     * @return 新增后的资产
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AstAsset addAsset(AssetDTO assetDTO) {
        AstAsset asset = new AstAsset();
        
        // 复制基本属性
        BeanUtils.copyProperties(assetDTO, asset);
        
        // 生成 UUID（全局唯一标识符）
        // 用于二维码生成和跨系统识别
        asset.setUuid(UUID.randomUUID().toString());
        
        // 初始化价值字段
        // 净值 = 原值（初始时未折旧）
        asset.setNetValue(assetDTO.getOriginalValue());
        // 累计折旧 = 0
        asset.setAccumulatedDepreciation(BigDecimal.ZERO);
        
        // 设置初始状态为闲置
        asset.setStatus(0);
        
        // 初始化乐观锁版本号
        asset.setVersion(0);
        
        // 设置折旧开始日期（购置日期的次月）
        if (assetDTO.getPurchaseDate() != null) {
            LocalDate purchaseDate = LocalDate.parse(assetDTO.getPurchaseDate());
            // 折旧从购置次月开始
            asset.setDepreciationStartDate(purchaseDate.plusMonths(1));
        }
        
        // 如果未指定残值率，从分类中获取
        if (asset.getResidualRate() == null && asset.getCategoryId() != null) {
            AstCategory category = categoryMapper.selectById(asset.getCategoryId());
            if (category != null) {
                asset.setResidualRate(category.getResidualRate());
            }
        }
        
        // 保存到数据库
        assetMapper.insert(asset);
        
        log.info("新增资产成功: id={}, uuid={}, name={}", asset.getId(), asset.getUuid(), asset.getAssetName());
        
        return asset;
    }

    /**
     * 修改资产信息
     * 
     * 注意事项：
     * - 使用乐观锁防止并发覆盖
     * - 原值修改需重新计算净值
     * 
     * @param assetDTO 资产信息
     * @return 修改后的资产
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AstAsset updateAsset(AssetDTO assetDTO) {
        if (assetDTO.getId() == null) {
            throw new BusinessException("资产ID不能为空");
        }
        
        // 查询原资产信息
        AstAsset existAsset = assetMapper.selectById(assetDTO.getId());
        if (existAsset == null) {
            throw new BusinessException("资产不存在");
        }
        
        // 复制可修改的属性
        AstAsset asset = new AstAsset();
        BeanUtils.copyProperties(assetDTO, asset);
        
        // 保留不可修改的字段
        asset.setUuid(existAsset.getUuid());  // UUID 不可修改
        asset.setVersion(existAsset.getVersion());  // 乐观锁版本号
        
        // 如果修改了原值，重新计算净值
        if (assetDTO.getOriginalValue() != null && !assetDTO.getOriginalValue().equals(existAsset.getOriginalValue())) {
            // 净值 = 新原值 - 累计折旧
            BigDecimal netValue = assetDTO.getOriginalValue().subtract(existAsset.getAccumulatedDepreciation());
            asset.setNetValue(netValue);
        }
        
        // 执行更新（乐观锁自动校验版本号）
        int rows = assetMapper.updateById(asset);
        if (rows == 0) {
            throw new BusinessException("资产已被其他用户修改，请刷新后重试");
        }
        
        log.info("修改资产成功: id={}", asset.getId());
        
        return assetMapper.selectById(asset.getId());
    }

    /**
     * 删除资产（逻辑删除）
     * 
     * @param id 资产ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAsset(Long id) {
        AstAsset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }
        
        // 检查资产状态，报废资产才能删除
        if (asset.getStatus() != 3) {
            throw new BusinessException("只有报废状态的资产才能删除");
        }
        
        // 执行逻辑删除（MyBatis-Plus 自动处理）
        assetMapper.deleteById(id);
        
        log.info("删除资产成功: id={}", id);
    }

    /**
     * 生成资产二维码
     * 
     * @param id 资产ID
     * @return 二维码 Base64 字符串
     */
    @Override
    public String generateQrCode(Long id) {
        AstAsset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }
        
        return QrCodeUtils.generateQrCodeBase64(asset.getUuid());
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
     * @param id 资产ID
     * @param status 新状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        AstAsset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }
        
        // 校验状态流转合法性
        validateStatusTransition(asset.getStatus(), status);
        
        // 更新状态
        LambdaUpdateWrapper<AstAsset> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AstAsset::getId, id)
                .eq(AstAsset::getVersion, asset.getVersion())  // 乐观锁
                .set(AstAsset::getStatus, status);
        
        int rows = assetMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("资产已被其他用户修改，请刷新后重试");
        }
        
        log.info("更新资产状态成功: id={}, oldStatus={}, newStatus={}", id, asset.getStatus(), status);
    }

    /**
     * 校验状态流转合法性
     * 
     * @param currentStatus 当前状态
     * @param newStatus 新状态
     */
    private void validateStatusTransition(Integer currentStatus, Integer newStatus) {
        // 报废是终态，不能变更
        if (currentStatus == 3) {
            throw new BusinessException("报废资产不能变更状态");
        }
        
        // 报废可以从任意状态流转
        if (newStatus == 3) {
            return;
        }
        
        // 其他状态流转规则
        switch (currentStatus) {
            case 0: // 闲置
                if (newStatus != 1) {
                    throw new BusinessException("闲置资产只能领用变为在用状态");
                }
                break;
            case 1: // 在用
                if (newStatus != 0 && newStatus != 2) {
                    throw new BusinessException("在用资产只能退库或报修");
                }
                break;
            case 2: // 维修中
                if (newStatus != 1) {
                    throw new BusinessException("维修中资产只能变为在用状态");
                }
                break;
        }
    }

    /**
     * 填充关联信息
     * 
     * @param asset 资产对象
     */
    private void fillRelatedInfo(AstAsset asset) {
        // 填充分类名称
        if (asset.getCategoryId() != null) {
            AstCategory category = categoryMapper.selectById(asset.getCategoryId());
            if (category != null) {
                asset.setCategoryName(category.getCategoryName());
            }
        }
        
        // TODO: 填充部门名称、使用人姓名
        // 需要注入 SysDeptMapper 和 SysUserMapper
    }
}
