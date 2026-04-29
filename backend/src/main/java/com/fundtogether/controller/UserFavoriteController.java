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

/**
 * 用户收藏控制器
 * <p>
 * 提供用户项目收藏相关的API功能，包括：
 * <ul>
 *   <li>添加收藏：用户收藏指定项目</li>
 *   <li>取消收藏：用户取消收藏指定项目</li>
 *   <li>查询收藏状态：判断当前用户是否已收藏指定项目</li>
 *   <li>查询收藏列表：分页查询当前用户收藏的项目列表</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/favorite")
public class UserFavoriteController {

    /** 用户收藏服务，处理收藏的增删查 */
    @Autowired
    private UserFavoriteService userFavoriteService;

    /**
     * 获取当前登录用户ID
     *
     * @return 当前登录用户的ID
     */
    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getId();
    }

    /**
     * 添加收藏
     * <p>
     * 用户收藏指定项目，重复收藏不会报错（幂等操作）。
     * </p>
     *
     * @param projectId 项目ID
     * @return 收藏结果提示
     */
    @PostMapping("/{projectId}")
    public Result<?> addFavorite(@PathVariable Long projectId) {
        userFavoriteService.addFavorite(getCurrentUserId(), projectId);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏
     *
     * @param projectId 项目ID
     * @return 取消收藏结果提示
     */
    @DeleteMapping("/{projectId}")
    public Result<?> removeFavorite(@PathVariable Long projectId) {
        userFavoriteService.removeFavorite(getCurrentUserId(), projectId);
        return Result.success("已取消收藏");
    }

    /**
     * 查询收藏状态
     * <p>
     * 判断当前用户是否已收藏指定项目，用于前端显示收藏按钮状态。
     * </p>
     *
     * @param projectId 项目ID
     * @return 包含favorited字段的Map，true表示已收藏
     */
    @GetMapping("/status/{projectId}")
    public Result<Map<String, Boolean>> checkFavorite(@PathVariable Long projectId) {
        boolean favorited = userFavoriteService.isFavorited(getCurrentUserId(), projectId);
        return Result.success(Map.of("favorited", favorited));
    }

    /**
     * 分页查询当前用户收藏的项目列表
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @return 分页项目列表（用户收藏的项目）
     */
    @GetMapping("/my-list")
    public Result<IPage<Project>> getMyFavorites(@RequestParam(defaultValue = "1") Integer current,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(userFavoriteService.getMyFavorites(getCurrentUserId(), current, size));
    }
}
