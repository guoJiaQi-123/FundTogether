package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户充值订单实体
 * <p>
 * 记录用户向平台钱包充值的订单信息。用户可通过支付渠道充值金额到平台钱包，
 * 充值成功后钱包余额增加，可用于支持众筹项目。
 * 对应数据库表：user_recharge_order
 * </p>
 */
@Data
@TableName("user_recharge_order")
public class UserRechargeOrder {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号，系统生成的唯一订单号 */
    private String orderNo;

    /** 充值用户ID，关联sys_user表 */
    private Long userId;

    /** 充值金额（单位：元） */
    private BigDecimal amount;

    /** 订单状态：0-待支付，1-已支付 */
    private Integer status;

    /** 支付完成时间 */
    private LocalDateTime payTime;

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
