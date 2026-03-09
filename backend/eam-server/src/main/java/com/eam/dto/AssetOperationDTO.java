package com.eam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 资产流转操作请求 DTO
 * 
 * 用于接收资产领用、退库、调拨、维修、报废等操作的请求参数
 * 
 * @author 毕业设计项目组
 */
@Data
@Schema(description = "资产流转操作请求参数")
public class AssetOperationDTO {

    /**
     * 资产ID
     */
    @Schema(description = "资产ID", required = true)
    private Long assetId;

    /**
     * 操作类型
     * 2-领用，3-退库，4-调拨，5-维修，6-报废
     */
    @Schema(description = "操作类型：2-领用，3-退库，4-调拨，5-维修，6-报废", required = true)
    private Integer operateType;

    /**
     * 目标部门ID（领用、调拨时使用）
     */
    @Schema(description = "目标部门ID（领用、调拨时使用）")
    private Long toDeptId;

    /**
     * 目标使用人ID（领用时使用）
     */
    @Schema(description = "目标使用人ID（领用时使用）")
    private Long toUserId;

    /**
     * 操作描述/申请理由
     */
    @Schema(description = "操作描述/申请理由")
    private String description;

    /**
     * 维修费用（报修时使用）
     */
    @Schema(description = "维修费用（报修时使用）")
    private java.math.BigDecimal maintenanceCost;

    /**
     * 报废原因（报废时使用）
     */
    @Schema(description = "报废原因（报废时使用）")
    private String scrapReason;
}
