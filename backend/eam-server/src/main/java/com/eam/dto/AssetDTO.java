package com.eam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产新增/修改请求 DTO
 * 
 * 用于接收前端资产表单提交的数据
 * 
 * @author 毕业设计项目组
 */
@Data
@Schema(description = "资产新增/修改请求参数")
public class AssetDTO {

    /**
     * 资产ID（修改时必填，新增时不填）
     */
    @Schema(description = "资产ID（修改时必填）")
    private Long id;

    /**
     * 资产编码
     */
    @Schema(description = "资产编码")
    private String assetCode;

    /**
     * 资产名称
     */
    @Schema(description = "资产名称", example = "联想ThinkPad笔记本电脑")
    private String assetName;

    /**
     * 资产分类ID
     */
    @Schema(description = "资产分类ID", example = "5")
    private Long categoryId;

    /**
     * 品牌
     */
    @Schema(description = "品牌", example = "联想")
    private String brand;

    /**
     * 型号规格
     */
    @Schema(description = "型号规格", example = "ThinkPad E14")
    private String model;

    /**
     * 详细规格参数
     */
    @Schema(description = "详细规格参数")
    private String specification;

    /**
     * 资产原值（元）
     */
    @Schema(description = "资产原值（元）", example = "5999.00")
    private BigDecimal originalValue;

    /**
     * 预计净残值率（%）
     */
    @Schema(description = "预计净残值率（%）", example = "5.00")
    private BigDecimal residualRate;

    /**
     * 购置日期
     */
    @Schema(description = "购置日期", example = "2026-03-01")
    private String purchaseDate;

    /**
     * 保修到期日期
     */
    @Schema(description = "保修到期日期")
    private String warrantyExpireDate;

    /**
     * 存放位置
     */
    @Schema(description = "存放位置", example = "A栋3楼财务部")
    private String location;

    /**
     * 来源类型
     */
    @Schema(description = "来源类型：1-采购入库，2-调拨入库，3-捐赠", example = "1")
    private Integer sourceType;

    /**
     * 供应商
     */
    @Schema(description = "供应商", example = "京东商城")
    private String supplier;

    /**
     * 发票号码
     */
    @Schema(description = "发票号码")
    private String invoiceNumber;

    /**
     * 资产图片URL
     */
    @Schema(description = "资产图片URL")
    private String imageUrl;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
