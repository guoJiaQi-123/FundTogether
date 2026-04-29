package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户收藏Mapper接口
 * <p>
 * 提供对user_favorite表的基础CRUD操作及自定义更新方法。
 * 包含恢复已取消收藏的方法，支持用户反复收藏/取消收藏同一项目。
 * </p>
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {

    /**
     * 恢复已取消的收藏记录
     * <p>
     * 当用户对之前已取消收藏的项目重新收藏时，不新增记录，
     * 而是将原有的逻辑删除记录恢复（将deleted字段从1改为0），
     * 避免产生重复的收藏记录。
     * </p>
     *
     * @param userId    用户ID
     * @param projectId 项目ID
     * @return 影响的行数，0表示恢复失败（无已取消的收藏记录）
     */
    @Update("UPDATE user_favorite SET deleted = 0 WHERE user_id = #{userId} AND project_id = #{projectId} AND deleted = 1")
    int restoreFavorite(@Param("userId") Long userId, @Param("projectId") Long projectId);
}
