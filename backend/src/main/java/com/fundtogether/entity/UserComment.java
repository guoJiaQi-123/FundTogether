package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户评论实体
 * <p>
 * 存储用户对项目的评论信息，支持一级评论（直接对项目评论）和二级评论（回复他人评论）。
 * 通过parentId字段实现评论的层级结构，支持点赞计数和状态管理。
 * 对应数据库表：user_comment
 * </p>
 */
@Data
@TableName("user_comment")
public class UserComment {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评论者用户ID，关联sys_user表 */
    private Long userId;

    /** 评论所属项目ID */
    private Long projectId;

    /** 父评论ID，为null时表示一级评论，非null时表示回复该评论 */
    private Long parentId;

    /** 评论正文内容 */
    private String content;

    /** 点赞数，该评论被点赞的次数 */
    private Integer likesCount;

    /** 评论状态：0-正常，1-已隐藏（管理员操作） */
    private Integer status;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 评论者昵称（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private String nickname;

    /** 评论者头像URL（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private String avatar;
}
