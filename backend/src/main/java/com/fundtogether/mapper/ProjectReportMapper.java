package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.ProjectReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目举报Mapper接口
 * <p>
 * 提供对project_report表的基础CRUD操作，
 * 继承MyBatis-Plus的BaseMapper，自动拥有增删改查能力。
 * </p>
 */
@Mapper
public interface ProjectReportMapper extends BaseMapper<ProjectReport> {
}
