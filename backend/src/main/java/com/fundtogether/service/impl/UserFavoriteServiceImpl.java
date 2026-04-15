package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.common.exception.BusinessException;
import com.fundtogether.entity.Project;
import com.fundtogether.entity.UserFavorite;
import com.fundtogether.mapper.UserFavoriteMapper;
import com.fundtogether.service.ProjectService;
import com.fundtogether.service.UserFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements UserFavoriteService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Override
    public void addFavorite(Long userId, Long projectId) {
        Project project = projectService.getById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId);
        wrapper.eq(UserFavorite::getProjectId, projectId);
        if (this.count(wrapper) > 0) {
            return;
        }

        int restored = userFavoriteMapper.restoreFavorite(userId, projectId);
        if (restored > 0) {
            return;
        }

        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setProjectId(projectId);
        this.save(favorite);
    }

    @Override
    public void removeFavorite(Long userId, Long projectId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId);
        wrapper.eq(UserFavorite::getProjectId, projectId);
        this.remove(wrapper);
    }

    @Override
    public boolean isFavorited(Long userId, Long projectId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId);
        wrapper.eq(UserFavorite::getProjectId, projectId);
        return this.count(wrapper) > 0;
    }

    @Override
    public IPage<Project> getMyFavorites(Long userId, Integer current, Integer size) {
        Page<UserFavorite> favPage = new Page<>(current, size);
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId);
        wrapper.orderByDesc(UserFavorite::getCreatedAt);
        IPage<UserFavorite> favResult = this.page(favPage, wrapper);

        Page<Project> projectPage = new Page<>(current, size, favResult.getTotal());
        java.util.List<Project> projects = favResult.getRecords().stream()
                .map(fav -> projectService.getById(fav.getProjectId()))
                .filter(p -> p != null)
                .collect(java.util.stream.Collectors.toList());
        projectPage.setRecords(projects);
        return projectPage;
    }
}
