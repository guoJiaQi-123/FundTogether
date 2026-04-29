package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现订单实体
 * <p>
 * 记录用户（项目发起人）从平台钱包提现资金的订单信息。
 * 用户发起提现申请后，管理员审核通过后完成打款。
 * 提现金额从用户钱包余额中扣除，转入用户绑定的收款账户。
 * 对应数据库表：withdrawal_order
 * </p>
 */
@Data
@TableName("withdrawal_order")
public class WithdrawalOrder {
    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号，系统生成的唯一订单号 */
    private String orderNo;

    /** 提现用户ID，关联sys_user表 */
    private Long userId;

    /** 提现金额（单位：元） */
    private BigDecimal amount;

    /** 提现方式/收款类型：1-微信，2-支付宝，3-银行卡 */
    private Integer type;

    /** 收款人姓名 */
    private String accountName;

    /** 收款账号 */
    private String accountNo;

    /** 开户银行名称，仅银行卡类型时使用 */
    private String bankName;

    /** 订单状态：0-待审核，1-已通过，2-已驳回 */
    private Integer status;

    /** 驳回原因，管理员驳回时填写的理由 */
    private String rejectReason;

    /** 处理时间，管理员审核通过或驳回的时间 */
    private LocalDateTime processTime;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 用户名称（非数据库字段，关联查询时填充） */
    @TableField(exist = false)
    private String userName;
}
