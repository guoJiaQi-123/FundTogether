package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.entity.SysUser;
import com.fundtogether.entity.UserFollow;
import com.fundtogether.mapper.SysUserMapper;
import com.fundtogether.mapper.UserFollowMapper;
import com.fundtogether.service.UserFollowService;
import com.fundtogether.vo.FollowStatusVO;
import com.fundtogether.vo.FollowUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserFollowService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String FOLLOWING_COUNT_KEY = "user:following:count:";
    private static final String FOLLOWER_COUNT_KEY = "user:follower:count:";
    private static final long COUNT_CACHE_TTL_HOURS = 2;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FollowStatusVO follow(Long userId, Long followUserId) {
        if (userId.equals(followUserId)) {
            throw new RuntimeException("不能关注自己");
        }

        SysUser targetUser = sysUserMapper.selectById(followUserId);
        if (targetUser == null) {
            throw new RuntimeException("目标用户不存在");
        }

        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
               .eq(UserFollow::getFollowUserId, followUserId);
        UserFollow existing = this.getOne(wrapper);

        if (existing != null) {
            throw new RuntimeException("已经关注了该用户");
        }

        UserFollow userFollow = new UserFollow();
        userFollow.setUserId(userId);
        userFollow.setFollowUserId(followUserId);
        this.save(userFollow);

        invalidateFollowCountCache(userId);
        invalidateFollowerCountCache(followUserId);

        log.info("用户关注操作: userId={}, followUserId={}", userId, followUserId);

        return buildFollowStatusVO(userId, followUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FollowStatusVO unfollow(Long userId, Long followUserId) {
        if (userId.equals(followUserId)) {
            throw new RuntimeException("不能取消关注自己");
        }

        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
               .eq(UserFollow::getFollowUserId, followUserId);
        UserFollow existing = this.getOne(wrapper);

        if (existing == null) {
            throw new RuntimeException("未关注该用户");
        }

        this.removeById(existing.getId());

        invalidateFollowCountCache(userId);
        invalidateFollowerCountCache(followUserId);

        log.info("用户取关操作: userId={}, unfollowUserId={}", userId, followUserId);

        return buildFollowStatusVO(userId, followUserId);
    }

    @Override
    public FollowStatusVO getFollowStatus(Long currentUserId, Long targetUserId) {
        return buildFollowStatusVO(currentUserId, targetUserId);
    }

    @Override
    public long getFollowingCount(Long userId) {
        String key = FOLLOWING_COUNT_KEY + userId;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Long.parseLong(cached);
        }

        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId);
        long count = this.count(wrapper);

        redisTemplate.opsForValue().set(key, String.valueOf(count), COUNT_CACHE_TTL_HOURS, TimeUnit.HOURS);
        return count;
    }

    @Override
    public long getFollowerCount(Long userId) {
        String key = FOLLOWER_COUNT_KEY + userId;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Long.parseLong(cached);
        }

        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowUserId, userId);
        long count = this.count(wrapper);

        redisTemplate.opsForValue().set(key, String.valueOf(count), COUNT_CACHE_TTL_HOURS, TimeUnit.HOURS);
        return count;
    }

    @Override
    public List<FollowUserVO> getFollowingList(Long userId, int page, int size) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
               .orderByDesc(UserFollow::getCreatedAt);

        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + size + " OFFSET " + offset);

        List<UserFollow> follows = this.list(wrapper);

        if (follows.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> followUserIds = follows.stream()
                .map(UserFollow::getFollowUserId)
                .collect(Collectors.toList());

        List<SysUser> users = sysUserMapper.selectBatchIds(followUserIds);
        Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        return follows.stream().map(f -> {
            FollowUserVO vo = new FollowUserVO();
            SysUser u = userMap.get(f.getFollowUserId());
            if (u != null) {
                vo.setId(u.getId());
                vo.setNickname(u.getNickname());
                vo.setAvatar(u.getAvatar());
                vo.setBio(u.getBio());
            }
            vo.setIsFollowing(true);
            vo.setFollowedAt(f.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<FollowUserVO> getFollowerList(Long userId, int page, int size) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowUserId, userId)
               .orderByDesc(UserFollow::getCreatedAt);

        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + size + " OFFSET " + offset);

        List<UserFollow> follows = this.list(wrapper);

        if (follows.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> followerUserIds = follows.stream()
                .map(UserFollow::getUserId)
                .collect(Collectors.toList());

        List<SysUser> users = sysUserMapper.selectBatchIds(followerUserIds);
        Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        Set<Long> currentUserFollowingIds = getCurrentFollowingIds(userId);

        return follows.stream().map(f -> {
            FollowUserVO vo = new FollowUserVO();
            SysUser u = userMap.get(f.getUserId());
            if (u != null) {
                vo.setId(u.getId());
                vo.setNickname(u.getNickname());
                vo.setAvatar(u.getAvatar());
                vo.setBio(u.getBio());
            }
            vo.setIsFollowing(currentUserFollowingIds.contains(f.getUserId()));
            vo.setFollowedAt(f.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void fillFollowCounts(Map<Long, long[]> countMap, List<Long> userIds) {
        for (Long uid : userIds) {
            if (!countMap.containsKey(uid)) {
                countMap.put(uid, new long[]{getFollowingCount(uid), getFollowerCount(uid)});
            }
        }
    }

    private Set<Long> getCurrentFollowingIds(Long userId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
               .select(UserFollow::getFollowUserId);
        return this.list(wrapper).stream()
                .map(UserFollow::getFollowUserId)
                .collect(Collectors.toSet());
    }

    private FollowStatusVO buildFollowStatusVO(Long currentUserId, Long targetUserId) {
        FollowStatusVO vo = new FollowStatusVO();

        if (currentUserId == null) {
            vo.setIsFollowing(false);
        } else {
            LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserFollow::getUserId, currentUserId)
                   .eq(UserFollow::getFollowUserId, targetUserId);
            vo.setIsFollowing(this.count(wrapper) > 0);
        }

        vo.setFollowingCount(getFollowingCount(targetUserId));
        vo.setFollowerCount(getFollowerCount(targetUserId));
        return vo;
    }

    private void invalidateFollowCountCache(Long userId) {
        redisTemplate.delete(FOLLOWING_COUNT_KEY + userId);
    }

    private void invalidateFollowerCountCache(Long userId) {
        redisTemplate.delete(FOLLOWER_COUNT_KEY + userId);
    }
}
