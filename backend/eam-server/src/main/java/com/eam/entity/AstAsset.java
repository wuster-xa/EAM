package com.eam.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资产主表实体类 - 核心中的核心
 * 
 * 对应数据库表：ast_asset
 * 存储资产完整信息，实现资产全生命周期状态管理
 * 
 * 核心设计亮点：
 * 1. UUID 全局唯一标识：用于二维码生成和跨系统识别
 * 2. Version 乐观锁版本号：防止高并发下的数据更新覆盖
 * 3. 价值字段：原值、净值、残值率、累计折旧
 * 4. 状态字段：闲置、在用、维修中、报废
 * 
 * @author 毕业设计项目组
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ast_asset")
public class AstAsset extends BaseEntity {

    private static final long serialVersionUID = 1L;

    // ==================== 唯一标识字段 ====================

    /**
     * 资产全局唯一标识符（UUID）
     * 
     * 用途：
     * 1. 生成二维码，支持移动端扫码盘点
     * 2. 跨系统识别资产，避免 ID 冲突
     * 
     * 格式：xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
     */
    private String uuid;

    /**
     * 资产编码
     * 企业内部编号，可自定义
     */
    private String assetCode;

    // ==================== 基本信息 ====================

    /**
     * 资产名称
     */
    private String assetName;

    /**
     * 资产分类ID
     * 关联 ast_category 表
     * 决定折旧年限和残值率
     */
    private Long categoryId;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 型号规格
     */
    private String model;

    /**
     * 详细规格参数
     */
    private String specification;

    // ==================== 价值相关字段（财务核心） ====================

    /**
     * 资产原值（元）
     * 购置时的初始价值
     */
    private BigDecimal originalValue;

    /**
     * 资产净值（元）
     * 原值减去累计折旧后的价值
     */
    private BigDecimal netValue;

    /**
     * 预计净残值率（%）
     * 从分类继承或单独设定
     * 如：5.00 表示 5%
     */
    private BigDecimal residualRate;

    /**
     * 累计折旧金额（元）
     * 所有已计提折旧的总和
     */
    private BigDecimal accumulatedDepreciation;

    // ==================== 时间相关字段 ====================

    /**
     * 购置日期
     */
    private LocalDate purchaseDate;

    /**
     * 保修到期日期
     */
    private LocalDate warrantyExpireDate;

    /**
     * 折旧开始日期
     * 通常为入库次月
     */
    private LocalDate depreciationStartDate;

    // ==================== 状态与位置字段 ====================

    /**
     * 资产状态
     * 0 - 闲置
     * 1 - 在用
     * 2 - 维修中
     * 3 - 报废
     */
    private Integer status;

    /**
     * 当前使用部门ID
     * 关联 sys_dept 表
     */
    private Long deptId;

    /**
     * 存放位置
     * 如：A栋3楼财务部
     */
    private String location;

    /**
     * 当前使用人ID
     * 关联 sys_user 表
     */
    private Long userId;

    // ==================== 来源与供应商 ====================

    /**
     * 来源类型
     * 1 - 采购入库
     * 2 - 调拨入库
     * 3 - 捐赠
     */
    private Integer sourceType;

    /**
     * 供应商名称
     */
    private String supplier;

    /**
     * 发票号码
     */
    private String invoiceNumber;

    // ==================== 图片与附件 ====================

    /**
     * 资产图片URL
     */
    private String imageUrl;

    // ==================== 并发控制（乐观锁） ====================

    /**
     * 乐观锁版本号
     * 
     * MyBatis-Plus 自动维护：
     * - 更新时自动在 WHERE 条件中添加 version = oldVersion
     * - 并将 SET 中的 version 设置为 oldVersion + 1
     * 
     * 作用：防止高并发场景下的数据更新覆盖问题
     */
    @Version
    private Integer version;

    // ==================== 其他 ====================

    /**
     * 备注说明
     */
    private String remark;

    // ==================== 非数据库字段（用于显示） ====================

    /**
     * 分类名称（非数据库字段）
     */
    @TableField(exist = false)
    private String categoryName;

    /**
     * 部门名称（非数据库字段）
     */
    @TableField(exist = false)
    private String deptName;

    /**
     * 使用人姓名（非数据库字段）
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 二维码 Base64 字符串（非数据库字段）
     * 用于前端展示二维码图片
     */
    @TableField(exist = false)
    private String qrCodeBase64;
}
