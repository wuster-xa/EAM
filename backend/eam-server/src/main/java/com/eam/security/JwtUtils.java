package com.eam.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 
 * 功能说明：
 * 1. 生成 JWT Token：将用户信息封装到 Token 中
 * 2. 解析 JWT Token：从 Token 中提取用户信息
 * 3. 验证 JWT Token：检查 Token 是否有效、是否过期
 * 
 * 技术要点：
 * - 使用 HMAC-SHA256 算法进行签名
 * - Token 包含：用户ID、用户名、角色信息
 * - 支持自定义过期时间
 * 
 * 无状态认证原理：
 * 服务器不存储 Session，每次请求携带 Token，
 * 服务器通过解析 Token 获取用户身份信息。
 * 
 * @author 毕业设计项目组
 */
@Slf4j
@Component
public class JwtUtils {

    /**
     * JWT 签名密钥
     * 建议在生产环境中使用 256 位以上的随机字符串
     */
    @Value("${jwt.secret:eam-system-jwt-secret-key-must-be-at-least-256-bits-long}")
    private String secret;

    /**
     * Token 过期时间（毫秒）
     * 默认 24 小时
     */
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    /**
     * Token 请求头名称
     */
    @Value("${jwt.header:Authorization}")
    private String header;

    /**
     * Token 前缀
     */
    @Value("${jwt.prefix:Bearer }")
    private String prefix;

    /**
     * 生成签名密钥
     * 
     * 使用 HMAC-SHA256 算法，密钥长度必须 >= 256 位
     * 
     * @return SecretKey 签名密钥
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token
     * 
     * Token 结构：
     * Header: { "alg": "HS256", "typ": "JWT" }
     * Payload: { "sub": "用户名", "userId": 用户ID, "role": "角色编码", "exp": 过期时间 }
     * Signature: HMAC-SHA256 签名
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param roleCode 角色编码
     * @return JWT Token 字符串
     */
    public String generateToken(Long userId, String username, String roleCode) {
        Map<String, Object> claims = new HashMap<>();
        // 存储用户ID
        claims.put("userId", userId);
        // 存储用户名
        claims.put("username", username);
        // 存储角色编码（用于权限校验）
        claims.put("role", roleCode);
        return createToken(claims, username);
    }

    /**
     * 创建 Token
     * 
     * @param claims 自定义声明信息
     * @param subject Token 主题（通常是用户名）
     * @return JWT Token 字符串
     */
    private String createToken(Map<String, Object> claims, String subject) {
        // 当前时间
        Date now = new Date();
        // 过期时间 = 当前时间 + 过期时长
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                // 设置自定义声明（用户ID、角色等）
                .claims(claims)
                // 设置主题（用户名）
                .subject(subject)
                // 设置签发时间
                .issuedAt(now)
                // 设置过期时间
                .expiration(expirationDate)
                // 使用 HMAC-SHA256 算法签名
                .signWith(getSignKey())
                // 压缩生成 Token 字符串
                .compact();
    }

    /**
     * 从 Token 中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("从Token获取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
            return null;
        } catch (Exception e) {
            log.error("从Token获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取角色编码
     * 
     * @param token JWT Token
     * @return 角色编码
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return (String) claims.get("role");
        } catch (Exception e) {
            log.error("从Token获取角色失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析 Token 获取声明信息
     * 
     * @param token JWT Token
     * @return Claims 声明信息
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                // 设置签名验证密钥
                .verifyWith(getSignKey())
                .build()
                // 解析并验证 Token
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 Token 是否有效
     * 
     * 验证流程：
     * 1. 解析 Token（验证签名）
     * 2. 检查是否过期
     * 
     * @param token JWT Token
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            // 检查是否过期
            return !isTokenExpired(claims);
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的Token格式: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token格式错误: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Token为空: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 检查 Token 是否过期
     * 
     * @param claims Token 声明信息
     * @return true-已过期，false-未过期
     */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 刷新 Token（重新生成）
     * 
     * @param token 旧 Token
     * @return 新 Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            Long userId = getUserIdFromToken(token);
            String username = claims.getSubject();
            String role = getRoleFromToken(token);
            return generateToken(userId, username, role);
        } catch (Exception e) {
            log.error("刷新Token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取请求头名称
     */
    public String getHeader() {
        return header;
    }

    /**
     * 获取 Token 前缀
     */
    public String getPrefix() {
        return prefix;
    }
}
