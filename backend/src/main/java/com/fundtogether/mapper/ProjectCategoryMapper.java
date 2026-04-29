package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.ProjectCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目分类Mapper接口
 * <p>
 * 提供对project_category表的基础CRUD操作，
 * 继承MyBatis-Plus的BaseMapper，自动拥有增删改查能力。
 * </p>
 */
@Mapper
public interface ProjectCategoryMapper extends BaseMapper<ProjectCategory> {
}
