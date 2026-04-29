package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户消息实体
 * <p>
 * 用于存储发送给用户的各类消息通知，包括系统公告、项目动态、评论回复和订单通知等。
 * 消息按类型分类，支持已读/未读状态管理，帮助用户及时了解平台动态。
 * 对应数据库表：sys_user_message
 * </p>
 */
@Data
@TableName("sys_user_message")
public class SysUserMessage {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息接收用户ID，关联sys_user表 */
    private Long userId;

    /**
     * 消息类型：
     * 1-系统公告（平台发布的公告通知）
     * 2-项目动态（关注项目的进展更新）
     * 3-评论回复（他人回复了用户的评论）
     * 4-订单及状态通知（支付成功、退款、发货等通知）
     */
    private Integer type;

    /** 消息标题 */
    private String title;

    /** 消息正文内容 */
    private String content;

    /** 关联业务ID，如项目ID、评论ID、订单ID等，用于点击消息后跳转到对应页面 */
    private Long relatedId;

    /** 是否已读：0-未读，1-已读 */
    private Integer isRead;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
