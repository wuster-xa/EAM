package com.eam.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 资产分类实体类
 * 
 * 对应数据库表：ast_category
 * 定义资产分类信息，决定折旧年限和残值率
 * 
 * @author 毕业设计项目组
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ast_category")
public class AstCategory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 父分类ID
     * 0 表示顶级分类
     */
    private Long parentId;

    /**
     * 分类名称
     * 如：电子设备、办公家具、运输工具
     */
    private String categoryName;

    /**
     * 分类编码
     * 如：ELECTRONIC、FURNITURE
     */
    private String categoryCode;

    /**
     * 折旧年限（年）
     * 用于计算折旧
     */
    private Integer depreciationYears;

    /**
     * 预计净残值率（%）
     * 如：5.00 表示 5%
     */
    private BigDecimal residualRate;

    /**
     * 显示排序号
     */
    private Integer sort;

    /**
     * 分类状态
     * 1 - 正常
     * 0 - 禁用
     */
    private Integer status;

    /**
     * 子分类列表（非数据库字段）
     */
    @TableField(exist = false)
    private java.util.List<AstCategory> children;
}
