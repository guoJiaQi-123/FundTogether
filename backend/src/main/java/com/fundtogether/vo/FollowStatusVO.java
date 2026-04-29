package com.fundtogether.vo;

import lombok.Data;

/**
 * 关注状态视图对象
 * <p>
 * 用于返回用户的关注关系状态信息，包括当前用户是否关注了目标用户，
 * 以及目标用户的关注数和粉丝数。通常在查看用户主页时返回。
 * </p>
 */
@Data
public class FollowStatusVO {
    /** 当前用户是否已关注目标用户 */
    private Boolean isFollowing;
    /** 目标用户的关注数（该用户关注了多少人） */
    private Long followingCount;
    /** 目标用户的粉丝数（有多少人关注了该用户） */
    private Long followerCount;
}
