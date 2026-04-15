package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
    @Update("UPDATE user_favorite SET deleted = 0 WHERE user_id = #{userId} AND project_id = #{projectId} AND deleted = 1")
    int restoreFavorite(@Param("userId") Long userId, @Param("projectId") Long projectId);
}
