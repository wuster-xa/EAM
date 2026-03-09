package com.eam.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * 
 * 功能说明：
 * 拦截所有 HTTP 请求，从请求头中提取 JWT Token，
 * 验证 Token 有效性，并将用户身份信息存入 SecurityContext。
 * 
 * 认证流程：
 * 1. 从请求头 Authorization 中提取 Token
 * 2. 验证 Token 是否有效
 * 3. 从 Token 中解析用户信息（用户ID、用户名、角色）
 * 4. 将用户信息封装为 Authentication 对象
 * 5. 存入 SecurityContext，供后续权限校验使用
 * 
 * 无状态认证优势：
 * - 服务器无需存储 Session，支持分布式部署
 * - 天然防范 CSRF 攻击
 * - 支持跨域请求
 * 
 * @author 毕业设计项目组
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    /**
     * 过滤器核心方法
     * 
     * 每个 HTTP 请求都会经过此方法进行认证
     * 
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. 从请求头中获取 Token
        String token = getTokenFromRequest(request);
        
        // 2. Token 存在且有效，则进行认证
        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            // 从 Token 中解析用户信息
            Long userId = jwtUtils.getUserIdFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);
            String roleCode = jwtUtils.getRoleFromToken(token);
            
            if (userId != null && username != null) {
                // 创建用户身份信息对象
                // 将角色编码封装为 Spring Security 的权限对象
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleCode);
                
                // 创建认证令牌
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                                userId,           // principal: 用户ID
                                null,             // credentials: 密码（Token认证不需要）
                                Collections.singletonList(authority)  // authorities: 权限列表
                        );
                
                // 将用户名存入认证对象的 details 中
                authentication.setDetails(username);
                
                // 存入 SecurityContext，供后续使用
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("JWT认证成功: userId={}, username={}, role={}", userId, username, roleCode);
            }
        }
        
        // 3. 继续执行后续过滤器
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 Token
     * 
     * Token 格式：Authorization: Bearer xxx.xxx.xxx
     * 
     * @param request HTTP 请求
     * @return Token 字符串（不含 Bearer 前缀），如果不存在则返回 null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 获取请求头
        String bearerToken = request.getHeader(jwtUtils.getHeader());
        
        // 检查是否以 Bearer 开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtUtils.getPrefix())) {
            // 去除 Bearer 前缀，返回纯 Token
            return bearerToken.substring(jwtUtils.getPrefix().length());
        }
        
        return null;
    }
}
