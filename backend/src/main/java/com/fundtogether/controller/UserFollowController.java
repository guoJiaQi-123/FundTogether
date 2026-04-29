package com.fundtogether.controller;

import com.fundtogether.common.Result;
import com.fundtogether.service.UserFollowService;
import com.fundtogether.vo.FollowStatusVO;
import com.fundtogether.vo.FollowUserVO;
import com.fundtogether.security.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户关注控制器
 * <p>
 * 提供用户关注/粉丝相关的API功能，包括：
 * <ul>
 *   <li>关注用户：当前用户关注指定用户</li>
 *   <li>取消关注：当前用户取消关注指定用户</li>
 *   <li>查询关注状态：判断当前用户与指定用户的关注关系</li>
 *   <li>查询关注列表：分页查询指定用户关注的人列表</li>
 *   <li>查询粉丝列表：分页查询指定用户的粉丝列表</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/follow")
public class UserFollowController {

    /** 用户关注服务，处理关注/取关及关注关系查询 */
    @Autowired
    private UserFollowService userFollowService;

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
     * 关注指定用户
     *
     * @param userId 被关注用户的ID
     * @return 关注状态信息，包含是否关注、关注数、粉丝数
     */
    @PostMapping("/{userId}")
    public Result<FollowStatusVO> follow(@PathVariable Long userId) {
        try {
            FollowStatusVO status = userFollowService.follow(getCurrentUserId(), userId);
            return Result.success(status);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消关注指定用户
     *
     * @param userId 被取消关注用户的ID
     * @return 关注状态信息，包含是否关注、关注数、粉丝数
     */
    @DeleteMapping("/{userId}")
    public Result<FollowStatusVO> unfollow(@PathVariable Long userId) {
        try {
            FollowStatusVO status = userFollowService.unfollow(getCurrentUserId(), userId);
            return Result.success(status);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询与指定用户的关注状态
     * <p>
     * 返回当前用户是否关注了指定用户，以及对方的关注数和粉丝数。
     * 未登录用户也可查询，此时isFollowed为false。
     * </p>
     *
     * @param userId 被查询用户的ID
     * @return 关注状态信息
     */
    @GetMapping("/status/{userId}")
    public Result<FollowStatusVO> getFollowStatus(@PathVariable Long userId) {
        Long currentUserId = null;
        try {
            currentUserId = getCurrentUserId();
        } catch (Exception ignored) {
            // 未登录用户也可查询关注状态，此时currentUserId为null
        }
        FollowStatusVO status = userFollowService.getFollowStatus(currentUserId, userId);
        return Result.success(status);
    }

    /**
     * 查询指定用户的关注列表
     * <p>
     * 分页查询指定用户关注的人的列表。
     * </p>
     *
     * @param userId 被查询用户的ID
     * @param page   当前页码，默认1
     * @param size   每页条数，默认20
     * @return 关注用户VO列表
     */
    @GetMapping("/following/{userId}")
    public Result<List<FollowUserVO>> getFollowingList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(userFollowService.getFollowingList(userId, page, size));
    }

    /**
     * 查询指定用户的粉丝列表
     * <p>
     * 分页查询关注指定用户的人的列表。
     * </p>
     *
     * @param userId 被查询用户的ID
     * @param page   当前页码，默认1
     * @param size   每页条数，默认20
     * @return 粉丝用户VO列表
     */
    @GetMapping("/followers/{userId}")
    public Result<List<FollowUserVO>> getFollowerList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(userFollowService.getFollowerList(userId, page, size));
    }
}
