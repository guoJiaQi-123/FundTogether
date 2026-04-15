package com.fundtogether.vo;

import lombok.Data;

@Data
public class FollowStatusVO {
    private Boolean isFollowing;
    private Long followingCount;
    private Long followerCount;
}
