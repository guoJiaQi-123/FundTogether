package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 项目动态更新实体
 * <p>
 * 用于记录项目发起人发布的进展动态。项目动态是发起人向支持者汇报项目进展的方式，
 * 可包含文字内容和配图，帮助支持者了解项目最新情况，增强项目透明度。
 * 对应数据库表：project_update
 * </p>
 */
@Data
@TableName("project_update")
public class ProjectUpdate {
    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的项目ID */
    private Long projectId;

    /** 动态标题 */
    private String title;

    /** 动态正文内容 */
    private String content;

    /** 动态配图，JSON格式存储多张图片URL */
    private String images;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
