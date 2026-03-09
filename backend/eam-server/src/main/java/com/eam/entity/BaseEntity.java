package com.eam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 
 * 包含所有实体类的公共字段：
 * - id: 主键ID（自增）
 * - isDeleted: 逻辑删除标识
 * - createTime: 创建时间（自动填充）
 * - updateTime: 更新时间（自动填充）
 * 
 * 使用方式：
 * 其他实体类继承此类即可获得公共字段
 * 
 * @author 毕业设计项目组
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 使用数据库自增策略
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 逻辑删除标识
     * 0 - 未删除
     * 1 - 已删除
     * 
     * MyBatis-Plus 会自动处理逻辑删除：
     * - 查询时自动添加 WHERE is_deleted = 0
     * - 删除时自动执行 UPDATE SET is_deleted = 1
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 创建时间
     * 插入时自动填充当前时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 插入和更新时自动填充当前时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
