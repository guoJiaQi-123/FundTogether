package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关注关系实体
 * <p>
 * 记录用户之间的关注关系。用户可关注其他用户（如项目发起人），
 * 方便及时获取关注对象的最新动态和项目信息。
 * 对应数据库表：user_follow
 * </p>
 */
@Data
@TableName("user_follow")
public class UserFollow {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发起关注的用户ID，关联sys_user表 */
    private Long userId;

    /** 被关注的用户ID，关联sys_user表 */
    private Long followUserId;

    /** 关注时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
