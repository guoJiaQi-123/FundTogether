package com.fundtogether.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fundtogether.common.Result;
import com.fundtogether.entity.Project;
import com.fundtogether.entity.UserFavorite;
import com.fundtogether.security.LoginUser;
import com.fundtogether.service.UserFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class UserFavoriteController {

    @Autowired
    private UserFavoriteService userFavoriteService;

    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getId();
    }

    @PostMapping("/{projectId}")
    public Result<?> addFavorite(@PathVariable Long projectId) {
        userFavoriteService.addFavorite(getCurrentUserId(), projectId);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/{projectId}")
    public Result<?> removeFavorite(@PathVariable Long projectId) {
        userFavoriteService.removeFavorite(getCurrentUserId(), projectId);
        return Result.success("已取消收藏");
    }

    @GetMapping("/status/{projectId}")
    public Result<Map<String, Boolean>> checkFavorite(@PathVariable Long projectId) {
        boolean favorited = userFavoriteService.isFavorited(getCurrentUserId(), projectId);
        return Result.success(Map.of("favorited", favorited));
    }

    @GetMapping("/my-list")
    public Result<IPage<Project>> getMyFavorites(@RequestParam(defaultValue = "1") Integer current,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userFavoriteService.getMyFavorites(getCurrentUserId(), current, size));
    }
}
