package com.eam.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户实体类
 * 
 * 对应数据库表：sys_user
 * 存储系统用户基本信息，支持部门归属以实现跨部门调拨
 * 
 * 字段说明：
 * - username: 用户登录账号，唯一
 * - password: 用户密码（BCrypt加密存储）
 * - realName: 用户真实姓名
 * - deptId: 所属部门ID，支持跨部门调拨场景
 * - status: 账号状态（1-正常，0-禁用）
 * 
 * @author 毕业设计项目组
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户登录账号
     * 唯一标识，用于登录认证
     */
    private String username;

    /**
     * 用户密码
     * 使用 BCrypt 算法加密存储
     * 格式：$2a$10$...
     */
    private String password;

    /**
     * 用户真实姓名
     * 用于显示和业务操作记录
     */
    private String realName;

    /**
     * 所属部门ID
     * 关联 sys_dept 表
     * 支持跨部门资产调拨场景
     */
    private Long deptId;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 账号状态
     * 1 - 正常启用
     * 0 - 禁用
     */
    private Integer status;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 角色编码（非数据库字段）
     * 从 Token 或关联表查询填充
     */
    @TableField(exist = false)
    private String roleCode;

    /**
     * 角色名称（非数据库字段）
     * 用于前端显示
     */
    @TableField(exist = false)
    private String roleName;

    /**
     * 部门名称（非数据库字段）
     * 用于前端显示
     */
    @TableField(exist = false)
    private String deptName;
}
