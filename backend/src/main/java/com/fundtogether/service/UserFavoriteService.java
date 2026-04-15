package com.fundtogether.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fundtogether.entity.UserFavorite;
import com.fundtogether.entity.Project;

public interface UserFavoriteService extends IService<UserFavorite> {
    void addFavorite(Long userId, Long projectId);
    void removeFavorite(Long userId, Long projectId);
    boolean isFavorited(Long userId, Long projectId);
    IPage<Project> getMyFavorites(Long userId, Integer current, Integer size);
}
