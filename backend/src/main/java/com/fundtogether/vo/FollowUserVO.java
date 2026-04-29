package com.fundtogether.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关注用户视图对象
 * <p>
 * 用于在关注列表中展示被关注用户的基本信息，
 * 包含用户的个人资料摘要以及关注状态和关注时间。
 * </p>
 */
@Data
public class FollowUserVO {
    /** 用户ID */
    private Long id;
    /** 用户昵称 */
    private String nickname;
    /** 用户头像URL */
    private String avatar;
    /** 用户个人简介 */
    private String bio;
    /** 当前登录用户是否已关注该用户 */
    private Boolean isFollowing;
    /** 关注时间，即当前用户关注该用户的时间 */
    private LocalDateTime followedAt;
}
