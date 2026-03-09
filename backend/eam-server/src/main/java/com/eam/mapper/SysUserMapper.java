package com.eam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eam.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper 接口
 * 
 * 继承 MyBatis-Plus 的 BaseMapper，自动拥有以下功能：
 * - insert(T entity): 插入一条记录
 * - deleteById(Serializable id): 根据 ID 删除
 * - updateById(T entity): 根据 ID 更新
 * - selectById(Serializable id): 根据 ID 查询
 * - selectList(Wrapper<T> queryWrapper): 条件查询列表
 * - selectPage(Page<T> page, Wrapper<T> queryWrapper): 分页查询
 * 
 * 无需编写 XML 文件即可完成基础 CRUD 操作
 * 
 * @author 毕业设计项目组
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    // BaseMapper 已提供基础 CRUD 方法
    // 如需自定义 SQL，可在此添加方法并在 XML 中实现
    
}
