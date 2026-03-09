package com.eam.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 资产操作记录实体类
 * 
 * 对应数据库表：ast_operation_record
 * 记录资产全生命周期的所有变动操作，实现责任可追溯
 * 
 * 操作类型：
 * 1-入库，2-领用，3-退库，4-调拨，5-维修，6-报废
 * 
 * @author 毕业设计项目组
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ast_operation_record")
public class AstOperationRecord extends BaseEntity {

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
     * 操作类型
     * 1-入库，2-领用，3-退库，4-调拨，5-维修，6-报废
     */
    private Integer operateType;

    /**
     * 原部门ID（调拨时使用）
     */
    private Long fromDeptId;

    /**
     * 目标部门ID（领用、调拨时使用）
     */
    private Long toDeptId;

    /**
     * 原使用人ID
     */
    private Long fromUserId;

    /**
     * 目标使用人ID
     */
    private Long toUserId;

    /**
     * 变更前状态
     */
    private Integer fromStatus;

    /**
     * 变更后状态
     */
    private Integer toStatus;

    /**
     * 操作描述说明
     */
    private String description;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 审批状态
     * 0-无需审批，1-待审批，2-已通过，3-已驳回
     */
    private Integer approvalStatus;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    private String approvalRemark;

    // ==================== 非数据库字段 ====================

    /**
     * 资产名称（非数据库字段）
     */
    @TableField(exist = false)
    private String assetName;

    /**
     * 原部门名称（非数据库字段）
     */
    @TableField(exist = false)
    private String fromDeptName;

    /**
     * 目标部门名称（非数据库字段）
     */
    @TableField(exist = false)
    private String toDeptName;

    /**
     * 原使用人姓名（非数据库字段）
     */
    @TableField(exist = false)
    private String fromUserName;

    /**
     * 目标使用人姓名（非数据库字段）
     */
    @TableField(exist = false)
    private String toUserName;
}
