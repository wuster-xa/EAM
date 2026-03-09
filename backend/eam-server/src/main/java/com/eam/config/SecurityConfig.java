package com.eam.config;

import com.eam.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 安全配置类
 * 
 * 功能说明：
 * 1. 配置 JWT 认证过滤器
 * 2. 配置无状态 Session 管理
 * 3. 配置权限校验规则
 * 4. 配置密码加密器
 * 5. 配置跨域访问
 * 
 * 安全设计要点：
 * - 无状态认证：不使用 Session，每次请求携带 Token
 * - RBAC 权限控制：基于角色的访问控制
 * - 密码加密：使用 BCrypt 单向加密
 * - 跨域支持：允许前端跨域访问 API
 * 
 * @author 毕业设计项目组
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // 启用方法级权限控制 @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 安全过滤器链配置
     * 
     * 配置说明：
     * 1. 禁用 CSRF：前后端分离架构，使用 Token 认证，无需 CSRF 保护
     * 2. 无状态 Session：不创建和使用 Session
     * 3. 权限规则：定义哪些接口需要认证，哪些可以匿名访问
     * 4. JWT 过滤器：在用户名密码认证过滤器之前执行
     * 
     * @param http HttpSecurity 配置对象
     * @return SecurityFilterChain 安全过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF 保护
            // 原因：使用 JWT Token 认证，不依赖 Cookie，天然防范 CSRF
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置跨域
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置 Session 管理：无状态
            // 原因：JWT 是无状态认证，服务器不存储 Session
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 配置权限规则
            .authorizeHttpRequests(auth -> auth
                // 允许匿名访问的接口（登录、注册、API文档等）
                .requestMatchers(
                    "/api/auth/login",           // 登录接口
                    "/api/auth/register",        // 注册接口
                    "/swagger-ui/**",            // Swagger UI
                    "/swagger-ui.html",          // Swagger UI 首页
                    "/v3/api-docs/**",           // OpenAPI 文档
                    "/doc.html",                 // knife4j 文档
                    "/webjars/**",               // 静态资源
                    "/favicon.ico"               // 网站图标
                ).permitAll()
                
                // 静态资源允许访问
                .requestMatchers("/static/**", "/upload/**").permitAll()
                
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            
            // 添加 JWT 认证过滤器
            // 在 UsernamePasswordAuthenticationFilter 之前执行
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * 密码加密器
     * 
     * 使用 BCrypt 算法进行密码加密：
     * - 单向加密，不可逆
     * - 自动加盐，相同密码生成不同密文
     * - 支持密码强度校验
     * 
     * @return PasswordEncoder 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 加密强度为 10（默认值，2^10 次迭代）
        return new BCryptPasswordEncoder();
    }

    /**
     * 跨域配置
     * 
     * 允许前端跨域访问后端 API：
     * - 允许所有来源（生产环境应限制为具体域名）
     * - 允许所有 HTTP 方法
     * - 允许携带认证信息（Cookie、Authorization 头）
     * 
     * @return CorsConfigurationSource 跨域配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的来源（开发环境允许所有，生产环境应限制）
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 允许的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许携带认证信息（Cookie、Authorization 头）
        configuration.setAllowCredentials(true);
        
        // 预检请求缓存时间（秒）
        configuration.setMaxAge(3600L);
        
        // 注册跨域配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
