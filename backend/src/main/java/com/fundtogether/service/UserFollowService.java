package com.fundtogether.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fundtogether.entity.UserFollow;
import com.fundtogether.vo.FollowUserVO;
import com.fundtogether.vo.FollowStatusVO;

import java.util.List;

public interface UserFollowService extends IService<UserFollow> {

    FollowStatusVO follow(Long userId, Long followUserId);

    FollowStatusVO unfollow(Long userId, Long followUserId);

    FollowStatusVO getFollowStatus(Long currentUserId, Long targetUserId);

    long getFollowingCount(Long userId);

    long getFollowerCount(Long userId);

    List<FollowUserVO> getFollowingList(Long userId, int page, int size);

    List<FollowUserVO> getFollowerList(Long userId, int page, int size);

    void fillFollowCounts(java.util.Map<Long, long[]> countMap, List<Long> userIds);
}
