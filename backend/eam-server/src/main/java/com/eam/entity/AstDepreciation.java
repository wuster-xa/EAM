package com.eam.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 折旧记录实体类
 * 
 * 对应数据库表：ast_depreciation
 * 记录每次自动折旧的详细信息，用于财务审计
 * 
 * @author 毕业设计项目组
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ast_depreciation")
public class AstDepreciation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * 资产UUID
     */
    private String assetUuid;

    /**
     * 资产名称
     */
    private String assetName;

    /**
     * 本次折旧时的资产原值
     */
    private BigDecimal originalValue;

    /**
     * 折旧前净值
     */
    private BigDecimal netValueBefore;

    /**
     * 本次折旧金额
     */
    private BigDecimal depreciationAmount;

    /**
     * 折旧后净值
     */
    private BigDecimal netValueAfter;

    /**
     * 折旧方法
     * STRAIGHT_LINE - 直线法
     * DOUBLE_DECLINING - 双倍余额递减法
     */
    private String depreciationMethod;

    /**
     * 折旧年度
     */
    private Integer depreciationYear;

    /**
     * 折旧月份
     */
    private Integer depreciationMonth;

    /**
     * 计算时使用的残值率
     */
    private BigDecimal residualRate;

    /**
     * 计算时使用的折旧年限
     */
    private Integer depreciationYears;

    /**
     * 折旧计算执行时间
     */
    private LocalDateTime executeTime;

    /**
     * 执行类型
     * 1 - 定时自动执行
     * 2 - 手动补录
     */
    private Integer executeType;

    /**
     * 备注
     */
    private String remark;
}
