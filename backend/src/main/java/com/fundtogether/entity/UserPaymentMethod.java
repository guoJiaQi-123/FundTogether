package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户支付方式实体
 * <p>
 * 存储用户的提现/收款账户信息，支持微信、支付宝和银行卡三种类型。
 * 用户发起提现时需选择一个已绑定的支付方式作为收款账户。
 * 对应数据库表：user_payment_method
 * </p>
 */
@Data
@TableName("user_payment_method")
public class UserPaymentMethod {
    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联用户ID，关联sys_user表 */
    private Long userId;

    /** 支付类型：1-微信，2-支付宝，3-银行卡 */
    private Integer type;

    /** 支付账号，微信/支付宝为对应账号，银行卡为卡号 */
    private String account;

    /** 持卡人/账户姓名 */
    private String name;

    /** 开户银行名称，仅银行卡类型时使用 */
    private String bankName;

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
