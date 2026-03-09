package com.eam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eam.dto.AssetOperationDTO;
import com.eam.dto.AssetQueryDTO;
import com.eam.entity.AstAsset;
import com.eam.entity.AstOperationRecord;
import com.eam.exception.BusinessException;
import com.eam.mapper.AstAssetMapper;
import com.eam.mapper.AstOperationRecordMapper;
import com.eam.service.AssetOperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 资产流转服务实现类
 * 
 * 实现资产全生命周期的流转操作：
 * - 领用：闲置 -> 在用
 * - 退库：在用 -> 闲置
 * - 调拨：部门间转移
 * - 维修：在用 -> 维修中 -> 在用
 * - 报废：任意 -> 报废
 * 
 * 状态机流转规则：
 * ┌─────────┐    领用    ┌─────────┐
 * │  闲置   │ ────────> │  在用   │
 * │  (0)    │ <─────── │  (1)    │
 * └─────────┘    退库    └─────────┘
 *                              │
 *                    报修      │      报废
 *                              ↓
 *                        ┌─────────┐
 *                        │ 维修中  │
 *                        │  (2)    │
 *                        └─────────┘
 *                              │
 *                    维修完成  │
 *                              ↓
 *                        ┌─────────┐
 *                        │  报废   │
 *                        │  (3)    │
 *                        └─────────┘
 * 
 * @author 毕业设计项目组
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetOperationServiceImpl implements AssetOperationService {

    private final AstAssetMapper assetMapper;
    private final AstOperationRecordMapper recordMapper;

    // 操作类型常量
    private static final int OPERATE_APPLY = 2;      // 领用
    private static final int OPERATE_RETURN = 3;     // 退库
    private static final int OPERATE_TRANSFER = 4;   // 调拨
    private static final int OPERATE_REPAIR = 5;     // 维修
    private static final int OPERATE_SCRAP = 6;      // 报废

    // 状态常量
    private static final int STATUS_IDLE = 0;        // 闲置
    private static final int STATUS_IN_USE = 1;      // 在用
    private static final int STATUS_REPAIRING = 2;   // 维修中
    private static final int STATUS_SCRAPPED = 3;    // 报废

    /**
     * 资产领用
     * 
     * 业务流程：
     * 1. 校验资产状态（必须为闲置）
     * 2. 更新资产状态、使用人、部门
     * 3. 记录操作日志
     * 
     * @param dto 操作请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(AssetOperationDTO dto) {
        // 1. 查询资产
        AstAsset asset = getAssetAndCheck(dto.getAssetId());

        // 2. 校验状态（只有闲置资产才能领用）
        if (asset.getStatus() != STATUS_IDLE) {
            throw new BusinessException("只有闲置状态的资产才能被领用，当前状态：" + getStatusName(asset.getStatus()));
        }

        // 3. 校验必填参数
        if (dto.getToDeptId() == null) {
            throw new BusinessException("领用部门不能为空");
        }
        if (dto.getToUserId() == null) {
            throw new BusinessException("领用人不能为空");
        }

        // 4. 更新资产状态
        LambdaUpdateWrapper<AstAsset> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AstAsset::getId, asset.getId())
                .eq(AstAsset::getVersion, asset.getVersion())
                .set(AstAsset::getStatus, STATUS_IN_USE)
                .set(AstAsset::getDeptId, dto.getToDeptId())
                .set(AstAsset::getUserId, dto.getToUserId());

        int rows = assetMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("资产已被其他用户修改，请刷新后重试");
        }

        // 5. 记录操作日志
        saveOperationRecord(asset, OPERATE_APPLY, null, dto.getToDeptId(), null, dto.getToUserId(),
                STATUS_IDLE, STATUS_IN_USE, dto.getDescription());

        log.info("资产领用成功: assetId={}, toDeptId={}, toUserId={}", asset.getId(), dto.getToDeptId(), dto.getToUserId());
    }

    /**
     * 资产退库
     * 
     * 业务流程：
     * 1. 校验资产状态（必须为在用）
     * 2. 清空使用人和部门信息
     * 3. 更新状态为闲置
     * 4. 记录操作日志
     * 
     * @param dto 操作请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnAsset(AssetOperationDTO dto) {
        // 1. 查询资产
        AstAsset asset = getAssetAndCheck(dto.getAssetId());

        // 2. 校验状态（只有在用资产才能退库）
        if (asset.getStatus() != STATUS_IN_USE) {
            throw new BusinessException("只有在用状态的资产才能退库，当前状态：" + getStatusName(asset.getStatus()));
        }

        // 3. 更新资产状态
        LambdaUpdateWrapper<AstAsset> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AstAsset::getId, asset.getId())
                .eq(AstAsset::getVersion, asset.getVersion())
                .set(AstAsset::getStatus, STATUS_IDLE)
                .set(AstAsset::getDeptId, null)
                .set(AstAsset::getUserId, null);

        int rows = assetMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("资产已被其他用户修改，请刷新后重试");
        }

        // 4. 记录操作日志
        saveOperationRecord(asset, OPERATE_RETURN, asset.getDeptId(), null, asset.getUserId(), null,
                STATUS_IN_USE, STATUS_IDLE, dto.getDescription());

        log.info("资产退库成功: assetId={}", asset.getId());
    }

    /**
     * 资产调拨
     * 
     * 业务流程：
     * 1. 校验资产状态（必须为在用）
     * 2. 变更部门和/或使用人
     * 3. 状态保持为在用
     * 4. 记录操作日志
     * 
     * @param dto 操作请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(AssetOperationDTO dto) {
        // 1. 查询资产
        AstAsset asset = getAssetAndCheck(dto.getAssetId());

        // 2. 校验状态（只有在用资产才能调拨）
        if (asset.getStatus() != STATUS_IN_USE) {
            throw new BusinessException("只有在用状态的资产才能调拨，当前状态：" + getStatusName(asset.getStatus()));
        }

        // 3. 校验是否为跨部门调拨
        boolean isCrossDept = dto.getToDeptId() != null && !dto.getToDeptId().equals(asset.getDeptId());
        if (isCrossDept) {
            log.info("跨部门调拨: assetId={}, fromDept={}, toDept={}", asset.getId(), asset.getDeptId(), dto.getToDeptId());
        }

        // 4. 更新资产信息
        LambdaUpdateWrapper<AstAsset> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AstAsset::getId, asset.getId())
                .eq(AstAsset::getVersion, asset.getVersion());

        if (dto.getToDeptId() != null) {
            updateWrapper.set(AstAsset::getDeptId, dto.getToDeptId());
        }
        if (dto.getToUserId() != null) {
            updateWrapper.set(AstAsset::getUserId, dto.getToUserId());
        }

        int rows = assetMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("资产已被其他用户修改，请刷新后重试");
        }

        // 5. 记录操作日志
        saveOperationRecord(asset, OPERATE_TRANSFER, asset.getDeptId(), dto.getToDeptId(),
                asset.getUserId(), dto.getToUserId(), STATUS_IN_USE, STATUS_IN_USE, dto.getDescription());

        log.info("资产调拨成功: assetId={}, toDeptId={}", asset.getId(), dto.getToDeptId());
    }

    /**
     * 资产报修
     * 
     * 业务流程：
     * 1. 校验资产状态（必须为在用）
     * 2. 更新状态为维修中
     * 3. 记录操作日志
     * 
     * @param dto 操作请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void repair(AssetOperationDTO dto) {
        // 1. 查询资产
        AstAsset asset = getAssetAndCheck(dto.getAssetId());

        // 2. 校验状态（只有在用资产才能报修）
        if (asset.getStatus() != STATUS_IN_USE) {
            throw new BusinessException("只有在用状态的资产才能报修，当前状态：" + getStatusName(asset.getStatus()));
        }

        // 3. 更新资产状态
        LambdaUpdateWrapper<AstAsset> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AstAsset::getId, asset.getId())
                .eq(AstAsset::getVersion, asset.getVersion())
                .set(AstAsset::getStatus, STATUS_REPAIRING);

        int rows = assetMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("资产已被其他用户修改，请刷新后重试");
        }

        // 4. 记录操作日志
        saveOperationRecord(asset, OPERATE_REPAIR, null, null, null, null,
                STATUS_IN_USE, STATUS_REPAIRING, dto.getDescription());

        log.info("资产报修成功: assetId={}", asset.getId());
    }

    /**
     * 资产报废
     * 
     * 业务流程：
     * 1. 校验资产状态（报废资产不能再报废）
     * 2. 更新状态为报废
     * 3. 报废后停止折旧
     * 4. 记录操作日志
     * 
     * @param dto 操作请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scrap(AssetOperationDTO dto) {
        // 1. 查询资产
        AstAsset asset = getAssetAndCheck(dto.getAssetId());

        // 2. 校验状态（报废资产不能再报废）
        if (asset.getStatus() == STATUS_SCRAPPED) {
            throw new BusinessException("资产已报废，不能重复报废");
        }

        // 3. 更新资产状态
        LambdaUpdateWrapper<AstAsset> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AstAsset::getId, asset.getId())
                .eq(AstAsset::getVersion, asset.getVersion())
                .set(AstAsset::getStatus, STATUS_SCRAPPED);

        int rows = assetMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("资产已被其他用户修改，请刷新后重试");
        }

        // 4. 记录操作日志
        saveOperationRecord(asset, OPERATE_SCRAP, null, null, null, null,
                asset.getStatus(), STATUS_SCRAPPED, dto.getScrapReason());

        log.info("资产报废成功: assetId={}", asset.getId());
    }

    /**
     * 维修完成
     * 
     * @param assetId 资产ID
     * @param description 维修结果描述
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeRepair(Long assetId, String description) {
        // 1. 查询资产
        AstAsset asset = getAssetAndCheck(assetId);

        // 2. 校验状态（只有维修中资产才能完成维修）
        if (asset.getStatus() != STATUS_REPAIRING) {
            throw new BusinessException("只有维修中状态的资产才能完成维修，当前状态：" + getStatusName(asset.getStatus()));
        }

        // 3. 更新资产状态
        LambdaUpdateWrapper<AstAsset> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AstAsset::getId, asset.getId())
                .eq(AstAsset::getVersion, asset.getVersion())
                .set(AstAsset::getStatus, STATUS_IN_USE);

        int rows = assetMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("资产已被其他用户修改，请刷新后重试");
        }

        // 4. 记录操作日志
        saveOperationRecord(asset, OPERATE_REPAIR, null, null, null, null,
                STATUS_REPAIRING, STATUS_IN_USE, "维修完成：" + description);

        log.info("资产维修完成: assetId={}", assetId);
    }

    /**
     * 查询资产操作记录
     * 
     * @param assetId 资产ID
     * @return 操作记录列表
     */
    @Override
    public java.util.List<AstOperationRecord> getOperationRecords(Long assetId) {
        LambdaQueryWrapper<AstOperationRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AstOperationRecord::getAssetId, assetId)
                .orderByDesc(AstOperationRecord::getOperateTime);
        return recordMapper.selectList(queryWrapper);
    }

    /**
     * 分页查询操作记录
     */
    @Override
    public Page<AstOperationRecord> queryRecordPage(Integer pageNum, Integer pageSize, Long assetId, Integer operateType) {
        Page<AstOperationRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AstOperationRecord> queryWrapper = new LambdaQueryWrapper<>();
        
        if (assetId != null) {
            queryWrapper.eq(AstOperationRecord::getAssetId, assetId);
        }
        if (operateType != null) {
            queryWrapper.eq(AstOperationRecord::getOperateType, operateType);
        }
        queryWrapper.orderByDesc(AstOperationRecord::getOperateTime);
        
        return recordMapper.selectPage(page, queryWrapper);
    }

    /**
     * 查询闲置资产列表（闲置超过30天）
     * 
     * 用于闲置资产盘活公示
     */
    @Override
    public Page<AstAsset> queryIdleAssets(AssetQueryDTO queryDTO) {
        Page<AstAsset> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<AstAsset> queryWrapper = new LambdaQueryWrapper<>();
        
        // 状态为闲置
        queryWrapper.eq(AstAsset::getStatus, STATUS_IDLE);
        
        // 更新时间超过30天
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        queryWrapper.lt(AstAsset::getUpdateTime, thirtyDaysAgo);
        
        // 其他筛选条件
        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq(AstAsset::getCategoryId, queryDTO.getCategoryId());
        }
        if (queryDTO.getAssetName() != null && !queryDTO.getAssetName().isEmpty()) {
            queryWrapper.like(AstAsset::getAssetName, queryDTO.getAssetName());
        }
        
        queryWrapper.orderByAsc(AstAsset::getUpdateTime);  // 闲置时间长的排前面
        
        return assetMapper.selectPage(page, queryWrapper);
    }

    /**
     * 申请调拨闲置资产
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyIdleAsset(AssetOperationDTO dto) {
        // 1. 查询资产
        AstAsset asset = getAssetAndCheck(dto.getAssetId());

        // 2. 校验状态
        if (asset.getStatus() != STATUS_IDLE) {
            throw new BusinessException("只有闲置资产才能申请调拨");
        }

        // 3. 校验闲置时间
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        if (asset.getUpdateTime().isAfter(thirtyDaysAgo)) {
            throw new BusinessException("资产闲置时间不足30天，不能申请调拨");
        }

        // 4. 校验必填参数
        if (dto.getToDeptId() == null) {
            throw new BusinessException("申请部门不能为空");
        }

        // 5. 更新资产状态
        LambdaUpdateWrapper<AstAsset> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AstAsset::getId, asset.getId())
                .eq(AstAsset::getVersion, asset.getVersion())
                .set(AstAsset::getStatus, STATUS_IN_USE)
                .set(AstAsset::getDeptId, dto.getToDeptId())
                .set(AstAsset::getUserId, dto.getToUserId());

        int rows = assetMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException("资产已被其他用户修改，请刷新后重试");
        }

        // 6. 记录操作日志
        saveOperationRecord(asset, OPERATE_TRANSFER, asset.getDeptId(), dto.getToDeptId(),
                asset.getUserId(), dto.getToUserId(), STATUS_IDLE, STATUS_IN_USE, 
                "闲置资产盘活调拨：" + dto.getDescription());

        log.info("闲置资产调拨成功: assetId={}, toDeptId={}", asset.getId(), dto.getToDeptId());
    }

    /**
     * 查询资产并校验
     */
    private AstAsset getAssetAndCheck(Long assetId) {
        if (assetId == null) {
            throw new BusinessException("资产ID不能为空");
        }
        AstAsset asset = assetMapper.selectById(assetId);
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }
        return asset;
    }

    /**
     * 保存操作记录
     */
    private void saveOperationRecord(AstAsset asset, Integer operateType,
            Long fromDeptId, Long toDeptId, Long fromUserId, Long toUserId,
            Integer fromStatus, Integer toStatus, String description) {
        
        AstOperationRecord record = new AstOperationRecord();
        record.setAssetId(asset.getId());
        record.setAssetUuid(asset.getUuid());
        record.setOperateType(operateType);
        record.setFromDeptId(fromDeptId);
        record.setToDeptId(toDeptId);
        record.setFromUserId(fromUserId);
        record.setToUserId(toUserId);
        record.setFromStatus(fromStatus);
        record.setToStatus(toStatus);
        record.setDescription(description);
        
        // 获取当前操作人
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            record.setOperatorId((Long) auth.getPrincipal());
            record.setOperatorName(auth.getDetails() != null ? auth.getDetails().toString() : "系统");
        }
        
        record.setOperateTime(LocalDateTime.now());
        record.setApprovalStatus(0);  // 无需审批
        
        recordMapper.insert(record);
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "闲置";
            case 1: return "在用";
            case 2: return "维修中";
            case 3: return "报废";
            default: return "未知";
        }
    }
}
