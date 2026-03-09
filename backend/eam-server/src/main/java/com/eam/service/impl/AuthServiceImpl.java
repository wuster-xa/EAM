package com.eam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eam.dto.LoginDTO;
import com.eam.dto.LoginVO;
import com.eam.entity.SysUser;
import com.eam.exception.BusinessException;
import com.eam.mapper.SysUserMapper;
import com.eam.security.JwtUtils;
import com.eam.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类
 * 
 * 实现登录认证的核心业务逻辑
 * 
 * 认证流程：
 * 1. 根据用户名查询用户信息
 * 2. 校验密码是否正确（BCrypt 比对）
 * 3. 校验账号状态是否正常
 * 4. 查询用户角色信息
 * 5. 生成 JWT Token
 * 6. 封装返回数据
 * 
 * @author 毕业设计项目组
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * 用户登录
     * 
     * 实现步骤：
     * 1. 根据用户名查询用户
     * 2. 校验用户是否存在
     * 3. 校验密码是否正确
     * 4. 校验账号状态
     * 5. 查询角色信息（简化版：直接从用户扩展字段获取）
     * 6. 生成 JWT Token
     * 7. 封装返回数据
     * 
     * @param loginDTO 登录请求参数
     * @return 登录响应数据
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, loginDTO.getUsername());
        SysUser user = sysUserMapper.selectOne(queryWrapper);

        // 2. 校验用户是否存在
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 校验密码是否正确
        // BCrypt 密码比对：将明文密码与密文密码进行比对
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 校验账号状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 5. 查询角色信息
        // 简化版：默认分配普通员工角色
        // 完整版应从 sys_user_role 和 sys_role 表查询
        String roleCode = "EMPLOYEE";
        String roleName = "普通员工";

        // TODO: 完整版需要从数据库查询角色信息
        // 这里简化处理，根据用户名判断角色
        if ("admin".equals(user.getUsername())) {
            roleCode = "SUPER_ADMIN";
            roleName = "超级管理员";
        }

        // 6. 生成 JWT Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), roleCode);

        // 7. 封装返回数据
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setRoleCode(roleCode);
        loginVO.setRoleName(roleName);
        loginVO.setDeptId(user.getDeptId());
        loginVO.setAvatar(user.getAvatar());

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        return loginVO;
    }

    /**
     * 获取当前登录用户信息
     * 
     * 从 SecurityContext 中获取当前登录用户的认证信息，
     * 然后查询数据库获取完整的用户信息。
     * 
     * @return 用户信息
     */
    @Override
    public LoginVO getCurrentUser() {
        // 从 SecurityContext 获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录或Token已失效");
        }

        // 获取用户ID（在 JwtAuthenticationFilter 中设置的 principal）
        Long userId = (Long) authentication.getPrincipal();
        
        // 查询用户完整信息
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 获取角色信息
        String roleCode = "EMPLOYEE";
        String roleName = "普通员工";
        if ("admin".equals(user.getUsername())) {
            roleCode = "SUPER_ADMIN";
            roleName = "超级管理员";
        }

        // 封装返回数据
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setRoleCode(roleCode);
        loginVO.setRoleName(roleName);
        loginVO.setDeptId(user.getDeptId());
        loginVO.setAvatar(user.getAvatar());

        return loginVO;
    }
}
