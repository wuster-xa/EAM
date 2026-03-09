package com.eam.controller;

import com.eam.common.Result;
import com.eam.dto.LoginDTO;
import com.eam.dto.LoginVO;
import com.eam.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * 提供登录认证相关的接口
 * 
 * 接口列表：
 * - POST /auth/login: 用户登录，获取 JWT Token
 * - GET /auth/info: 获取当前登录用户信息
 * 
 * @author 毕业设计项目组
 */
@Tag(name = "认证管理", description = "登录认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * 
     * 接口说明：
     * - 接收用户名和密码
     * - 校验成功后返回 JWT Token 和用户信息
     * - 前端需在后续请求的 Authorization 头中携带 Token
     * 
     * @param loginDTO 登录请求参数
     * @return 登录响应数据（包含 Token 和用户信息）
     */
    @Operation(summary = "用户登录", description = "通过用户名密码登录，获取JWT Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 获取当前登录用户信息
     * 
     * 接口说明：
     * - 从请求头 Token 中解析用户身份
     * - 返回当前登录用户的详细信息
     * 
     * @return 用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "从Token中解析当前登录用户信息")
    @GetMapping("/info")
    public Result<LoginVO> getCurrentUser() {
        LoginVO loginVO = authService.getCurrentUser();
        return Result.success(loginVO);
    }

    /**
     * 退出登录
     * 
     * 接口说明：
     * - JWT 是无状态认证，服务端不存储 Session
     * - 退出登录由前端处理（清除本地 Token）
     * - 此接口仅作为语义标识
     * 
     * @return 成功响应
     */
    @Operation(summary = "退出登录", description = "退出登录（前端清除Token即可）")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("退出成功", null);
    }
}
