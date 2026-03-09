package com.eam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eam.entity.AstAsset;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资产主表 Mapper 接口
 * 
 * 继承 MyBatis-Plus 的 BaseMapper，自动拥有基础 CRUD 功能
 * 
 * @author 毕业设计项目组
 */
@Mapper
public interface AstAssetMapper extends BaseMapper<AstAsset> {

    // BaseMapper 已提供基础 CRUD 方法
    // 如需自定义 SQL，可在此添加方法并在 XML 中实现
    
}
