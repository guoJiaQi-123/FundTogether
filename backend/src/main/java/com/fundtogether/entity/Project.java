package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 众筹项目实体
 * <p>
 * 平台核心业务实体，存储众筹项目的完整信息，包括项目基本信息、内容描述、
 * 筹款目标与进度、状态流转等。项目由发起人创建，需经管理员审核后上线筹资。
 * 对应数据库表：project
 * </p>
 */
@Data
@TableName("project")
public class Project {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 项目发起人ID，关联sys_user表 */
    private Long sponsorId;

    /** 项目所属分类ID，关联project_category表 */
    private Long categoryId;

    /** 项目标题 */
    private String title;

    /** 项目简介，用于列表页和搜索结果展示的简短描述 */
    private String summary;

    /** 项目封面图片URL地址 */
    private String coverImage;

    /** 项目宣传视频URL地址 */
    private String videoUrl;

    /** 项目详情，富文本格式的完整描述内容 */
    private String content;

    /** 众筹目标金额（单位：元） */
    private BigDecimal targetAmount;

    /** 当前已筹金额（单位：元），随支持订单支付成功而累加 */
    private BigDecimal currentAmount;

    /** 支持者人数，累计支持该项目的用户数量 */
    private Integer supporterCount;

    /** 众筹开始时间 */
    private LocalDateTime startTime;

    /** 众筹截止时间，超过此时间后将无法继续筹资 */
    private LocalDateTime endTime;

    /**
     * 项目状态：
     * 0-待审核（刚创建，等待管理员审核）
     * 1-筹款中（审核通过，正在筹资）
     * 2-已驳回（审核未通过）
     * 3-已取消（发起人主动取消）
     * 4-已下架（管理员强制下架）
     * 5-已完成（筹资成功并结束）
     */
    private Integer status;

    /** 审核原因（非数据库字段），审核通过/驳回/下架时填写的原因 */
    @TableField(exist = false)
    private String auditReason;

    /** 项目热度值，用于排序推荐，值越高排名越靠前 */
    private Integer heat;

    /** 发起人昵称（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private String sponsorName;

    /** 发起人头像URL（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private String sponsorAvatar;

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
