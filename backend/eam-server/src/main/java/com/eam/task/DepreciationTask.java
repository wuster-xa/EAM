package com.eam.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eam.entity.AstAsset;
import com.eam.entity.AstCategory;
import com.eam.entity.AstDepreciation;
import com.eam.mapper.AstAssetMapper;
import com.eam.mapper.AstCategoryMapper;
import com.eam.mapper.AstDepreciationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产折旧定时任务
 * 
 * 功能说明：
 * 每月自动计算在用资产的折旧金额，更新资产净值，记录折旧历史
 * 
 * 折旧算法：
 * 采用平均年限法（直线法），公式：
 * 月折旧额 = (原值 - 预计净残值) / 折旧年限 / 12
 * 预计净残值 = 原值 × 残值率
 * 
 * 执行时间：
 * 每月1日凌晨0点5分执行
 * 
 * 业务规则：
 * 1. 只计算状态为"在用"和"闲置"的资产
 * 2. 报废资产停止折旧
 * 3. 净值不低于预计净残值
 * 4. 折旧年限已满的资产停止折旧
 * 
 * @author 毕业设计项目组
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepreciationTask {

    private final AstAssetMapper assetMapper;
    private final AstCategoryMapper categoryMapper;
    private final AstDepreciationMapper depreciationMapper;

    /**
     * 执行月度折旧计算
     * 
     * Cron 表达式：0 5 0 1 * ?
     * - 秒：0
     * - 分：5
     * - 时：0（凌晨）
     * - 日：1（每月1号）
     * - 月：*（每月）
     * - 周：?（不指定）
     * 
     * 执行流程：
     * 1. 查询需要折旧的资产
     * 2. 逐个计算折旧金额
     * 3. 更新资产净值和累计折旧
     * 4. 记录折旧历史
     */
    @Scheduled(cron = "0 5 0 1 * ?")
    @Transactional(rollbackFor = Exception.class)
    public void executeDepreciation() {
        log.info("========== 开始执行月度折旧计算 ==========");
        long startTime = System.currentTimeMillis();
        
        // 获取当前年月
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        
        // 检查本月是否已执行过折旧
        if (isDepreciationExecuted(year, month)) {
            log.info("本月折旧已执行，跳过");
            return;
        }
        
        // 查询需要折旧的资产
        // 状态为在用(1)或闲置(0)的资产
        LambdaQueryWrapper<AstAsset> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AstAsset::getStatus, 0, 1)  // 闲置或在用
                .isNotNull(AstAsset::getDepreciationStartDate)  // 已设置折旧开始日期
                .le(AstAsset::getDepreciationStartDate, today);  // 折旧开始日期 <= 当前日期
        
        List<AstAsset> assets = assetMapper.selectList(queryWrapper);
        log.info("待折旧资产数量: {}", assets.size());
        
        int successCount = 0;
        int skipCount = 0;
        
        for (AstAsset asset : assets) {
            try {
                // 计算折旧
                BigDecimal depreciationAmount = calculateDepreciation(asset, year, month);
                
                if (depreciationAmount == null || depreciationAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    skipCount++;
                    continue;
                }
                
                // 更新资产净值和累计折旧
                updateAssetDepreciation(asset, depreciationAmount);
                
                // 记录折旧历史
                saveDepreciationRecord(asset, depreciationAmount, year, month);
                
                successCount++;
                
            } catch (Exception e) {
                log.error("资产折旧计算失败: assetId={}, error={}", asset.getId(), e.getMessage(), e);
            }
        }
        
        long endTime = System.currentTimeMillis();
        log.info("========== 折旧计算完成 ========== 成功: {}, 跳过: {}, 耗时: {}ms", 
                successCount, skipCount, (endTime - startTime));
    }

    /**
     * 检查本月是否已执行过折旧
     * 
     * @param year 年份
     * @param month 月份
     * @return true-已执行，false-未执行
     */
    private boolean isDepreciationExecuted(int year, int month) {
        LambdaQueryWrapper<AstDepreciation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AstDepreciation::getDepreciationYear, year)
                .eq(AstDepreciation::getDepreciationMonth, month);
        Long count = depreciationMapper.selectCount(queryWrapper);
        return count != null && count > 0;
    }

    /**
     * 计算单月折旧金额
     * 
     * 平均年限法（直线法）计算公式：
     * 1. 预计净残值 = 原值 × 残值率
     * 2. 应计折旧总额 = 原值 - 预计净残值
     * 3. 月折旧额 = 应计折旧总额 / 折旧年限 / 12
     * 
     * @param asset 资产
     * @param year 年份
     * @param month 月份
     * @return 月折旧金额，如果无需折旧返回 null
     */
    private BigDecimal calculateDepreciation(AstAsset asset, int year, int month) {
        // 获取折旧参数
        BigDecimal originalValue = asset.getOriginalValue();
        BigDecimal residualRate = asset.getResidualRate();
        Integer depreciationYears = getDepreciationYears(asset);
        
        // 参数校验
        if (originalValue == null || originalValue.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (depreciationYears == null || depreciationYears <= 0) {
            return null;
        }
        if (residualRate == null) {
            residualRate = BigDecimal.valueOf(5);  // 默认残值率 5%
        }
        
        // 计算预计净残值
        BigDecimal residualValue = originalValue.multiply(residualRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // 检查净值是否已达到残值
        if (asset.getNetValue().compareTo(residualValue) <= 0) {
            log.debug("资产净值已达残值，停止折旧: assetId={}", asset.getId());
            return null;
        }
        
        // 检查是否已超过折旧年限
        LocalDate startDate = asset.getDepreciationStartDate();
        long monthsSinceStart = java.time.temporal.ChronoUnit.MONTHS.between(startDate, LocalDate.of(year, month, 1));
        long totalMonths = depreciationYears * 12L;
        
        if (monthsSinceStart >= totalMonths) {
            log.debug("资产已超过折旧年限，停止折旧: assetId={}", asset.getId());
            return null;
        }
        
        // 计算月折旧额
        BigDecimal depreciableAmount = originalValue.subtract(residualValue);
        BigDecimal monthlyDepreciation = depreciableAmount
                .divide(BigDecimal.valueOf(totalMonths), 2, RoundingMode.HALF_UP);
        
        // 确保折旧后净值不低于残值
        BigDecimal netValueAfterDepreciation = asset.getNetValue().subtract(monthlyDepreciation);
        if (netValueAfterDepreciation.compareTo(residualValue) < 0) {
            // 调整为最后一次折旧金额
            monthlyDepreciation = asset.getNetValue().subtract(residualValue);
        }
        
        return monthlyDepreciation;
    }

    /**
     * 获取资产折旧年限
     * 
     * 优先使用资产自身的折旧年限，否则从分类获取
     * 
     * @param asset 资产
     * @return 折旧年限
     */
    private Integer getDepreciationYears(AstAsset asset) {
        // 从分类获取折旧年限
        if (asset.getCategoryId() != null) {
            AstCategory category = categoryMapper.selectById(asset.getCategoryId());
            if (category != null && category.getDepreciationYears() != null) {
                return category.getDepreciationYears();
            }
        }
        return 5;  // 默认5年
    }

    /**
     * 更新资产净值和累计折旧
     * 
     * @param asset 资产
     * @param depreciationAmount 折旧金额
     */
    private void updateAssetDepreciation(AstAsset asset, BigDecimal depreciationAmount) {
        // 计算新净值
        BigDecimal newNetValue = asset.getNetValue().subtract(depreciationAmount);
        // 计算新累计折旧
        BigDecimal newAccumulatedDepreciation = asset.getAccumulatedDepreciation().add(depreciationAmount);
        
        // 更新数据库
        LambdaUpdateWrapper<AstAsset> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AstAsset::getId, asset.getId())
                .eq(AstAsset::getVersion, asset.getVersion())  // 乐观锁
                .set(AstAsset::getNetValue, newNetValue)
                .set(AstAsset::getAccumulatedDepreciation, newAccumulatedDepreciation);
        
        int rows = assetMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new RuntimeException("资产折旧更新失败，可能被其他操作修改");
        }
        
        log.debug("资产折旧更新成功: assetId={}, depreciation={}, newNetValue={}", 
                asset.getId(), depreciationAmount, newNetValue);
    }

    /**
     * 保存折旧记录
     * 
     * @param asset 资产
     * @param depreciationAmount 折旧金额
     * @param year 年份
     * @param month 月份
     */
    private void saveDepreciationRecord(AstAsset asset, BigDecimal depreciationAmount, int year, int month) {
        AstDepreciation record = new AstDepreciation();
        record.setAssetId(asset.getId());
        record.setAssetUuid(asset.getUuid());
        record.setAssetName(asset.getAssetName());
        record.setOriginalValue(asset.getOriginalValue());
        record.setNetValueBefore(asset.getNetValue());
        record.setDepreciationAmount(depreciationAmount);
        record.setNetValueAfter(asset.getNetValue().subtract(depreciationAmount));
        record.setDepreciationMethod("STRAIGHT_LINE");
        record.setDepreciationYear(year);
        record.setDepreciationMonth(month);
        record.setResidualRate(asset.getResidualRate());
        record.setDepreciationYears(getDepreciationYears(asset));
        record.setExecuteTime(LocalDateTime.now());
        record.setExecuteType(1);  // 自动执行
        
        depreciationMapper.insert(record);
    }
}
