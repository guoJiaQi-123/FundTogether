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

@RestController
@RequestMapping("/api/follow")
public class UserFollowController {

    @Autowired
    private UserFollowService userFollowService;

    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getId();
    }

    @PostMapping("/{userId}")
    public Result<FollowStatusVO> follow(@PathVariable Long userId) {
        try {
            FollowStatusVO status = userFollowService.follow(getCurrentUserId(), userId);
            return Result.success(status);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public Result<FollowStatusVO> unfollow(@PathVariable Long userId) {
        try {
            FollowStatusVO status = userFollowService.unfollow(getCurrentUserId(), userId);
            return Result.success(status);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/status/{userId}")
    public Result<FollowStatusVO> getFollowStatus(@PathVariable Long userId) {
        Long currentUserId = null;
        try {
            currentUserId = getCurrentUserId();
        } catch (Exception ignored) {
        }
        FollowStatusVO status = userFollowService.getFollowStatus(currentUserId, userId);
        return Result.success(status);
    }

    @GetMapping("/following/{userId}")
    public Result<List<FollowUserVO>> getFollowingList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(userFollowService.getFollowingList(userId, page, size));
    }

    @GetMapping("/followers/{userId}")
    public Result<List<FollowUserVO>> getFollowerList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(userFollowService.getFollowerList(userId, page, size));
    }
}
