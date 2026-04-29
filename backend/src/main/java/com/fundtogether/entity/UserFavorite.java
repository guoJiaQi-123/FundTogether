package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户收藏实体
 * <p>
 * 记录用户对项目的收藏（关注）关系。用户可收藏感兴趣的项目，
 * 方便后续在"我的收藏"中快速查看项目动态。
 * 每个用户对同一项目只能收藏一次。
 * 对应数据库表：user_favorite
 * </p>
 */
@Data
@TableName("user_favorite")
public class UserFavorite {
    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 收藏用户ID，关联sys_user表 */
    private Long userId;

    /** 收藏的项目ID */
    private Long projectId;

    /** 收藏时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
