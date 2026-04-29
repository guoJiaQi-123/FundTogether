package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户关注关系Mapper接口
 * <p>
 * 提供对user_follow表的基础CRUD操作，
 * 继承MyBatis-Plus的BaseMapper，自动拥有增删改查能力。
 * </p>
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
}
