package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 项目阶段拨付实体
 * <p>
 * 用于管理众筹项目筹资成功后的资金分阶段拨付给发起人的计划。
 * 平台将筹款金额按比例分阶段拨付，降低项目风险，保障支持者权益。
 * 如首期拨付30%、中期拨付40%、尾期拨付30%等。
 * 对应数据库表：project_payout
 * </p>
 */
@Data
@TableName("project_payout")
public class ProjectPayout {
    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的项目ID */
    private Long projectId;

    /** 项目发起人ID，即资金拨付的接收人 */
    private Long sponsorId;

    /** 拨付阶段：1-首期，2-中期，3-尾期等 */
    private Integer stage;

    /** 本阶段拨付金额（单位：元） */
    private BigDecimal amount;

    /** 本阶段拨付比例，如0.30表示30% */
    private BigDecimal payoutRatio;

    /** 拨付状态：0-待拨付，1-已拨付 */
    private Integer status;

    /** 拨付条件描述，如"项目启动后"、"交付中期成果后"等 */
    private String conditionDesc;

    /** 实际拨付时间 */
    private LocalDateTime payoutTime;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 项目名称（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private String projectName;

    /** 发起人姓名（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private String sponsorName;
}
