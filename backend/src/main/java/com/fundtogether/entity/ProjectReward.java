package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 项目回报（奖励档位）实体
 * <p>
 * 用于定义众筹项目的支持回报方案，每个项目可设置多个不同金额的回报档位。
 * 支持者选择不同档位支持项目后，项目成功时可获得对应的回报内容。
 * 对应数据库表：project_reward
 * </p>
 */
@Data
@TableName("project_reward")
public class ProjectReward {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的项目ID */
    private Long projectId;

    /** 回报档位金额（单位：元），支持者需支付此金额才能获得该档位回报 */
    private BigDecimal amount;

    /** 回报内容描述，如"限量版T恤+项目感谢信" */
    private String content;

    /** 回报限量数量，即该档位最多支持多少人选择 */
    @TableField("reward_count")
    private Integer limitCount;

    /** 当前已选择该档位的人数（非数据库字段，统计查询时填充） */
    @TableField(exist = false)
    private Integer currentCount;

    /** 预计回报发放时间（天数），如30表示项目成功后30天内发放 */
    @TableField("delivery_time")
    private Integer returnTime;

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
