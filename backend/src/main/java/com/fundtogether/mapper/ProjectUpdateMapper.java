package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.ProjectUpdate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目动态更新Mapper接口
 * <p>
 * 提供对project_update表的基础CRUD操作，
 * 继承MyBatis-Plus的BaseMapper，自动拥有增删改查能力。
 * </p>
 */
@Mapper
public interface ProjectUpdateMapper extends BaseMapper<ProjectUpdate> {
}
