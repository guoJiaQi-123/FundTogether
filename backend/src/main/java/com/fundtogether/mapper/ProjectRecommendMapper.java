package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.ProjectRecommend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 项目推荐Mapper接口
 * <p>
 * 提供对project_recommend表的基础CRUD操作及自定义查询。
 * 包含查询推荐列表并关联项目信息的业务方法，
 * 用于前台首页展示推荐项目。
 * </p>
 */
@Mapper
public interface ProjectRecommendMapper extends BaseMapper<ProjectRecommend> {

    /**
     * 查询所有推荐项目及其关联的项目信息
     * <p>
     * 左连接project表获取推荐项目的标题、封面、简介、目标金额、已筹金额、
     * 支持者数量、状态和截止时间等信息，按排序序号和创建时间排序。
     * </p>
     *
     * @return 包含项目信息的推荐列表
     */
    @Select("SELECT pr.*, p.title as project_title, p.cover_image, p.summary, " +
            "p.target_amount, p.current_amount, p.supporter_count, p.status, p.end_time " +
            "FROM project_recommend pr " +
            "LEFT JOIN project p ON pr.project_id = p.id AND p.deleted = 0 " +
            "WHERE pr.deleted = 0 " +
            "ORDER BY pr.sort_order ASC, pr.created_at DESC")
    List<ProjectRecommend> listWithProjectInfo();

    /**
     * 查询状态为"筹款中"的推荐项目及其关联的项目信息
     * <p>
     * 与listWithProjectInfo类似，但额外过滤项目状态为1（筹款中），
     * 并支持限制返回数量，用于前台首页展示有限数量的推荐项目。
     * </p>
     *
     * @param limit 返回的最大记录数
     * @return 包含项目信息的推荐列表（仅筹款中的项目）
     */
    @Select("SELECT pr.*, p.title as project_title, p.cover_image, p.summary, " +
            "p.target_amount, p.current_amount, p.supporter_count, p.status, p.end_time " +
            "FROM project_recommend pr " +
            "LEFT JOIN project p ON pr.project_id = p.id AND p.deleted = 0 " +
            "WHERE pr.deleted = 0 AND p.status = 1 " +
            "ORDER BY pr.sort_order ASC, pr.created_at DESC " +
            "LIMIT #{limit}")
    List<ProjectRecommend> listActiveWithProjectInfo(Integer limit);
}
