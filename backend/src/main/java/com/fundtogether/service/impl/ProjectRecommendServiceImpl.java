package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.entity.Project;
import com.fundtogether.entity.ProjectRecommend;
import com.fundtogether.mapper.ProjectRecommendMapper;
import com.fundtogether.service.ProjectRecommendService;
import com.fundtogether.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectRecommendServiceImpl extends ServiceImpl<ProjectRecommendMapper, ProjectRecommend> implements ProjectRecommendService {

    @Autowired
    private ProjectService projectService;

    @Override
    public List<ProjectRecommend> listWithProjectInfo() {
        return baseMapper.listWithProjectInfo();
    }

    @Override
    public List<ProjectRecommend> listActiveWithProjectInfo(Integer limit) {
        return baseMapper.listActiveWithProjectInfo(limit);
    }

    @Override
    public void addRecommend(Long projectId, Integer sortOrder) {
        Project project = projectService.getById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        LambdaQueryWrapper<ProjectRecommend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectRecommend::getProjectId, projectId);
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("该项目已在推荐列表中");
        }

        ProjectRecommend recommend = new ProjectRecommend();
        recommend.setProjectId(projectId);
        if (sortOrder != null) {
            recommend.setSortOrder(sortOrder);
        } else {
            Integer maxSort = lambdaQuery()
                    .orderByDesc(ProjectRecommend::getSortOrder)
                    .last("LIMIT 1")
                    .oneOpt()
                    .map(ProjectRecommend::getSortOrder)
                    .orElse(0);
            recommend.setSortOrder(maxSort + 1);
        }
        this.save(recommend);
    }

    @Override
    public void removeRecommend(Long id) {
        ProjectRecommend recommend = this.getById(id);
        if (recommend == null) {
            throw new RuntimeException("推荐记录不存在");
        }
        this.removeById(id);
    }

    @Override
    public void updateSortOrder(Long id, Integer sortOrder) {
        ProjectRecommend recommend = this.getById(id);
        if (recommend == null) {
            throw new RuntimeException("推荐记录不存在");
        }
        recommend.setSortOrder(sortOrder);
        this.updateById(recommend);
    }

    @Override
    public void batchUpdateSortOrder(List<Long> orderedIds) {
        for (int i = 0; i < orderedIds.size(); i++) {
            ProjectRecommend recommend = this.getById(orderedIds.get(i));
            if (recommend != null) {
                recommend.setSortOrder(i + 1);
                this.updateById(recommend);
            }
        }
    }
}
