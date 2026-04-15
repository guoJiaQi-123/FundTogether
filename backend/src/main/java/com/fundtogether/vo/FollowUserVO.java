package com.fundtogether.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowUserVO {
    private Long id;
    private String nickname;
    private String avatar;
    private String bio;
    private Boolean isFollowing;
    private LocalDateTime followedAt;
}
