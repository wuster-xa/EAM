package com.eam.service;

import com.eam.dto.LoginDTO;
import com.eam.dto.LoginVO;

/**
 * 认证服务接口
 * 
 * 定义认证相关的业务方法
 * 
 * @author 毕业设计项目组
 */
public interface AuthService {

    /**
     * 用户登录
     * 
     * 业务流程：
     * 1. 根据用户名查询用户
     * 2. 校验密码是否正确
     * 3. 查询用户角色信息
     * 4. 生成 JWT Token
     * 5. 封装返回数据
     * 
     * @param loginDTO 登录请求参数
     * @return 登录响应数据（包含 Token 和用户信息）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取当前登录用户信息
     * 
     * 从 SecurityContext 中获取当前登录用户的详细信息
     * 
     * @return 用户信息
     */
    LoginVO getCurrentUser();
}
