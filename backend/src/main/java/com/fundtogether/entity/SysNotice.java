package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统公告实体
 * <p>
 * 用于管理平台发布的系统公告和通知，管理员可创建公告并控制发布状态。
 * 已发布的公告将在前台展示给所有用户，用于传达平台动态、规则变更等信息。
 * 对应数据库表：sys_notice
 * </p>
 */
@Data
@TableName("sys_notice")
public class SysNotice {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告正文内容 */
    private String content;

    /**
     * 发布状态：
     * 0-未发布（草稿状态，仅管理员可见）
     * 1-已发布（前台对所有用户可见）
     */
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
}
