package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 项目分类实体
 * <p>
 * 用于管理众筹项目的分类体系，如"科技"、"设计"、"公益"等。
 * 每个项目可归属一个分类，支持排序和图标展示。
 * 对应数据库表：project_category
 * </p>
 */
@Data
@TableName("project_category")
public class ProjectCategory {
    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名称，如"科技"、"设计"、"公益"等 */
    private String name;

    /** 分类图标URL地址 */
    private String icon;

    /** 排序序号，值越小越靠前 */
    private Integer sort;

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
