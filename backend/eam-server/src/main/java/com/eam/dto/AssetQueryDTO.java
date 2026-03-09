package com.eam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 资产查询请求 DTO
 * 
 * 用于接收前端资产列表查询的筛选条件
 * 
 * @author 毕业设计项目组
 */
@Data
@Schema(description = "资产查询请求参数")
public class AssetQueryDTO {

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页记录数
     */
    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize = 10;

    /**
     * 资产名称（模糊查询）
     */
    @Schema(description = "资产名称（模糊查询）")
    private String assetName;

    /**
     * 资产编码（模糊查询）
     */
    @Schema(description = "资产编码（模糊查询）")
    private String assetCode;

    /**
     * 资产分类ID
     */
    @Schema(description = "资产分类ID")
    private Long categoryId;

    /**
     * 资产状态
     * 0-闲置，1-在用，2-维修中，3-报废
     */
    @Schema(description = "资产状态：0-闲置，1-在用，2-维修中，3-报废")
    private Integer status;

    /**
     * 使用部门ID
     */
    @Schema(description = "使用部门ID")
    private Long deptId;

    /**
     * 使用人ID
     */
    @Schema(description = "使用人ID")
    private Long userId;

    /**
     * 品牌（模糊查询）
     */
    @Schema(description = "品牌（模糊查询）")
    private String brand;

    /**
     * 存放位置（模糊查询）
     */
    @Schema(description = "存放位置（模糊查询）")
    private String location;
}
