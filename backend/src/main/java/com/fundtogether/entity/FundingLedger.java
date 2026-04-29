package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金流水账本实体
 * <p>
 * 记录平台所有资金流动的明细，包括用户支付、平台退款、阶段拨付和发起人提现等类型。
 * 是平台资金管理的核心实体，确保每一笔资金变动都有据可查。
 * 对应数据库表：funding_ledger
 * </p>
 */
@Data
@TableName("funding_ledger")
public class FundingLedger {
    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的项目ID */
    private Long projectId;

    /** 关联的订单ID，如支持订单ID、提现订单ID等 */
    private Long orderId;

    /** 操作用户ID */
    private Long userId;

    /** 资金变动金额（单位：元） */
    private BigDecimal amount;

    /** 交易后账户余额（单位：元） */
    private BigDecimal balanceAfter;

    /** 流水类型：1-用户支付，2-平台退款，3-阶段拨付给发起人，4-发起人提现，5-用户充值 */
    private Integer type;

    /** 流水状态：0-处理中，1-成功，2-失败 */
    private Integer status;

    /** 备注说明 */
    private String remark;

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

    /** 用户名称（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private String userName;
}
