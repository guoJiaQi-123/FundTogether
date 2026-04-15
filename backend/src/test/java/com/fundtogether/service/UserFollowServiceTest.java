package com.fundtogether.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fundtogether.entity.SysUser;
import com.fundtogether.entity.UserFollow;
import com.fundtogether.mapper.SysUserMapper;
import com.fundtogether.mapper.UserFollowMapper;
import com.fundtogether.vo.FollowStatusVO;
import com.fundtogether.vo.FollowUserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserFollowServiceTest {

    @Mock
    private UserFollowMapper userFollowMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private com.fundtogether.service.impl.UserFollowServiceImpl userFollowService;

    private SysUser testUser1;
    private SysUser testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = new SysUser();
        testUser1.setId(1L);
        testUser1.setNickname("User1");
        testUser1.setAvatar("avatar1.png");
        testUser1.setBio("Bio1");

        testUser2 = new SysUser();
        testUser2.setId(2L);
        testUser2.setNickname("User2");
        testUser2.setAvatar("avatar2.png");
        testUser2.setBio("Bio2");

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(valueOperations.get(anyString())).thenReturn(null);
    }

    @Test
    void follow_self_shouldThrow() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userFollowService.follow(1L, 1L));
        assertEquals("不能关注自己", ex.getMessage());
    }

    @Test
    void follow_nonExistentUser_shouldThrow() {
        when(sysUserMapper.selectById(999L)).thenReturn(null);
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userFollowService.follow(1L, 999L));
        assertEquals("目标用户不存在", ex.getMessage());
    }

    @Test
    void follow_alreadyFollowing_shouldThrow() {
        when(sysUserMapper.selectById(2L)).thenReturn(testUser2);
        when(userFollowMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new UserFollow());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userFollowService.follow(1L, 2L));
        assertEquals("已经关注了该用户", ex.getMessage());
    }

    @Test
    void unfollow_self_shouldThrow() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userFollowService.unfollow(1L, 1L));
        assertEquals("不能取消关注自己", ex.getMessage());
    }

    @Test
    void unfollow_notFollowing_shouldThrow() {
        when(userFollowMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                userFollowService.unfollow(1L, 2L));
        assertEquals("未关注该用户", ex.getMessage());
    }

    @Test
    void getFollowingCount_returnsCorrectCount() {
        when(userFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        long count = userFollowService.getFollowingCount(1L);
        assertEquals(5L, count);
    }

    @Test
    void getFollowerCount_returnsCorrectCount() {
        when(userFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);
        long count = userFollowService.getFollowerCount(1L);
        assertEquals(3L, count);
    }

    @Test
    void getFollowingCount_usesCache() {
        when(valueOperations.get("user:following:count:1")).thenReturn("10");
        long count = userFollowService.getFollowingCount(1L);
        assertEquals(10L, count);
        verify(userFollowMapper, never()).selectCount(any());
    }

    @Test
    void getFollowStatus_notFollowing() {
        when(userFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        FollowStatusVO status = userFollowService.getFollowStatus(1L, 2L);
        assertFalse(status.getIsFollowing());
    }

    @Test
    void getFollowStatus_isFollowing() {
        when(userFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        FollowStatusVO status = userFollowService.getFollowStatus(1L, 2L);
        assertTrue(status.getIsFollowing());
    }

    @Test
    void getFollowingList_emptyList() {
        when(userFollowMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        List<FollowUserVO> list = userFollowService.getFollowingList(1L, 1, 20);
        assertTrue(list.isEmpty());
    }

    @Test
    void getFollowerList_returnsCorrectData() {
        UserFollow follow = new UserFollow();
        follow.setId(1L);
        follow.setUserId(2L);
        follow.setFollowUserId(1L);

        when(userFollowMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(follow));
        when(sysUserMapper.selectBatchIds(any())).thenReturn(Arrays.asList(testUser2));
        when(userFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        List<FollowUserVO> list = userFollowService.getFollowerList(1L, 1, 20);
        assertEquals(1, list.size());
        assertEquals("User2", list.get(0).getNickname());
        assertFalse(list.get(0).getIsFollowing());
    }
}
