package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.UserComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户评论Mapper接口
 * <p>
 * 提供对user_comment表的基础CRUD操作，
 * 继承MyBatis-Plus的BaseMapper，自动拥有增删改查能力。
 * </p>
 */
@Mapper
public interface UserCommentMapper extends BaseMapper<UserComment> {
}
