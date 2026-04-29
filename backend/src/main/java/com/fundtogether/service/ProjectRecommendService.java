package com.fundtogether.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fundtogether.entity.ProjectRecommend;

import java.util.List;

public interface ProjectRecommendService extends IService<ProjectRecommend> {

    List<ProjectRecommend> listWithProjectInfo();

    List<ProjectRecommend> listActiveWithProjectInfo(Integer limit);

    void addRecommend(Long projectId, Integer sortOrder);

    void removeRecommend(Long id);

    void updateSortOrder(Long id, Integer sortOrder);

    void batchUpdateSortOrder(List<Long> orderedIds);
}
