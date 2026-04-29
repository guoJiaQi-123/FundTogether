package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 项目举报实体
 * <p>
 * 用于记录用户对项目的举报信息。用户可对违规或虚假项目进行举报，
 * 管理员审核后给出处理结果，保障平台内容质量和用户权益。
 * 对应数据库表：project_report
 * </p>
 */
@Data
@TableName("project_report")
public class ProjectReport {
    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 举报人ID，关联sys_user表 */
    private Long reporterId;

    /** 被举报的项目ID */
    private Long projectId;

    /** 举报原因，用户填写的举报理由 */
    private String reason;

    /** 处理状态：0-待处理，1-已处理 */
    private Integer status;

    /** 处理结果，管理员填写的处理说明 */
    private String handleResult;

    /** 处理管理员ID，关联sys_user表 */
    private Long handlerId;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 举报人姓名（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private String reporterName;

    /** 项目名称（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private String projectName;
}
