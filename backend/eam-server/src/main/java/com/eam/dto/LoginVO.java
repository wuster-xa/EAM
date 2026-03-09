package com.eam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录响应 DTO
 * 
 * 用于返回登录成功后的用户信息和 Token
 * 
 * @author 毕业设计项目组
 */
@Data
@Schema(description = "登录响应数据")
public class LoginVO {

    /**
     * JWT Token
     * 前端需在后续请求的 Authorization 头中携带此 Token
     */
    @Schema(description = "JWT Token")
    private String token;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    private String realName;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码")
    private String roleCode;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long deptId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String deptName;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatar;
}
