package com.eam.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 配置类
 * 
 * 功能说明：
 * 配置 API 文档的基本信息和认证方式
 * 访问地址：http://localhost:8080/swagger-ui.html
 * 
 * @author 毕业设计项目组
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置 OpenAPI 基本信息
     * 
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // API 基本信息
                .info(new Info()
                        .title("企业固定资产全生命周期管理系统 API")
                        .description("基于 Spring Boot + Vue 的企业固定资产全生命周期管理系统后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("毕业设计项目组")
                                .email("eam@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // 全局安全要求（所有接口默认需要 JWT 认证）
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                // 安全方案配置（JWT Token）
                .schemaRequirement("Bearer", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Token 认证，格式：Bearer {token}"));
    }
}
