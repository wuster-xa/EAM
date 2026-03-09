package com.eam.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置类
 * 
 * 核心功能：
 * 1. 分页插件：自动拦截分页查询，无需手动编写分页SQL
 * 2. 乐观锁插件：拦截 @Version 注解字段，自动维护版本号，防止并发更新覆盖
 * 3. 自动填充：自动填充 create_time、update_time 字段
 * 
 * 技术要点：
 * - 乐观锁原理：更新时自动在 WHERE 条件中添加 version = oldVersion，
 *   并将 SET 中的 version 设置为 oldVersion + 1
 * - 自动填充原理：在插入/更新时，自动设置字段值
 * 
 * @author 毕业设计项目组
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器配置
     * 
     * 配置说明：
     * 1. PaginationInnerInterceptor：分页插件，支持多种数据库类型
     * 2. OptimisticLockerInnerInterceptor：乐观锁插件，防止并发更新数据覆盖
     * 
     * 注意：插件添加顺序有要求，建议：多租户 -> 动态表名 -> 分页 -> 乐观锁 -> SQL性能规范 -> 防全表更新
     * 
     * @return MybatisPlusInterceptor 拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件配置
        // 指定数据库类型为 MySQL，确保分页SQL语法正确
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置单页最大记录数限制，防止恶意请求
        paginationInterceptor.setMaxLimit(500L);
        // 溢出总页数后是否进行处理（true返回第一页，false继续请求）
        paginationInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        // 乐观锁插件配置
        // 用于解决高并发场景下的数据更新覆盖问题
        // 使用方式：在实体类字段上添加 @Version 注解
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        return interceptor;
    }

    /**
     * 元数据自动填充处理器
     * 
     * 功能说明：
     * - 插入时自动填充 create_time、update_time
     * - 更新时自动填充 update_time
     * 
     * 使用方式：
     * 在实体类字段上添加注解：
     * - @TableField(fill = FieldFill.INSERT) - 插入时填充
     * - @TableField(fill = FieldFill.UPDATE) - 更新时填充
     * - @TableField(fill = FieldFill.INSERT_UPDATE) - 插入和更新时都填充
     * 
     * @return MetaObjectHandler 自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            
            /**
             * 插入时自动填充
             * 当执行 INSERT 操作时，自动设置 create_time 和 update_time
             * 
             * @param metaObject 元数据对象
             */
            @Override
            public void insertFill(MetaObject metaObject) {
                // 自动填充创建时间
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                // 自动填充更新时间
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

            /**
             * 更新时自动填充
             * 当执行 UPDATE 操作时，自动更新 update_time
             * 
             * @param metaObject 元数据对象
             */
            @Override
            public void updateFill(MetaObject metaObject) {
                // 自动填充更新时间
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
