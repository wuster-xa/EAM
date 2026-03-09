package com.eam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 企业固定资产全生命周期管理系统 - 启动类
 * 
 * 功能说明：
 * 1. @SpringBootApplication: Spring Boot 自动配置，包含组件扫描、自动配置
 * 2. @MapperScan: 扫描 MyBatis Mapper 接口，自动生成代理实现类
 * 3. @EnableScheduling: 启用定时任务支持，用于每月自动折旧计算
 * 
 * @author 毕业设计项目组
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.eam.mapper")
@EnableScheduling
public class EamServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EamServerApplication.class, args);
        System.out.println("==========================================");
        System.out.println("   企业固定资产全生命周期管理系统启动成功！   ");
        System.out.println("   API文档地址: http://localhost:8080/swagger-ui.html");
        System.out.println("==========================================");
    }
}
