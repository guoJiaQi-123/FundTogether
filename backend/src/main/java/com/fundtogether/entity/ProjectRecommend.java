package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 项目推荐实体
 * <p>
 * 用于管理平台首页或推荐位的精选项目展示。
 * 管理员可将优质项目设置为推荐项目，并配置排序顺序，
 * 帮助优质项目获得更多曝光和关注。
 * 对应数据库表：project_recommend
 * </p>
 */
@Data
@TableName("project_recommend")
public class ProjectRecommend {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 推荐关联的项目ID */
    private Long projectId;

    /** 排序序号，值越小越靠前展示 */
    private Integer sortOrder;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 项目标题（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private String projectTitle;

    /** 项目封面图片URL（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private String coverImage;

    /** 项目简介（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private String summary;

    /** 众筹目标金额（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private BigDecimal targetAmount;

    /** 当前已筹金额（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private BigDecimal currentAmount;

    /** 支持者人数（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private Integer supporterCount;

    /** 项目状态（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private Integer status;

    /** 众筹截止时间（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private LocalDateTime endTime;
}
