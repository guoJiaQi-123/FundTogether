package com.fundtogether.controller;

import com.fundtogether.common.Result;
import com.fundtogether.entity.SysUser;
import com.fundtogether.service.SysUserService;
import com.fundtogether.service.UserFollowService;
import com.fundtogether.vo.PublicUserProfileVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户公开信息控制器
 * <p>
 * 提供无需登录即可访问的用户公开信息API，包括：
 * <ul>
 *   <li>查询用户公开资料：获取指定用户的公开个人信息（昵称、头像、简介、关注数、粉丝数等）</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/public/user")
public class UserPublicController {

    /** 用户服务，用于查询用户基本信息 */
    @Autowired
    private SysUserService sysUserService;

    /** 用户关注服务，用于查询用户的关注数和粉丝数 */
    @Autowired
    private UserFollowService userFollowService;

    /**
     * 查询指定用户的公开资料
     * <p>
     * 返回用户的公开信息（不包含密码、邮箱等敏感信息），
     * 同时包含该用户的关注数和粉丝数统计。
     * </p>
     *
     * @param id 用户ID
     * @return 用户公开资料VO，包含昵称、头像、简介、关注数、粉丝数等
     */
    @GetMapping("/{id}")
    public Result<PublicUserProfileVO> getPublicUserProfile(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 将用户实体属性复制到公开资料VO（排除敏感字段）
        PublicUserProfileVO vo = new PublicUserProfileVO();
        BeanUtils.copyProperties(user, vo);
        // 补充关注数和粉丝数统计
        vo.setFollowingCount(userFollowService.getFollowingCount(id));
        vo.setFollowerCount(userFollowService.getFollowerCount(id));
        return Result.success(vo);
    }
}
