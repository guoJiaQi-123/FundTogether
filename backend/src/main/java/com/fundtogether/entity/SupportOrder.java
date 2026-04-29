package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支持订单实体
 * <p>
 * 记录用户对众筹项目的支持（付款）订单信息。
 * 用户选择项目并支付后生成支持订单，包含支付金额、状态、物流等信息。
 * 支持订单与项目回报档位关联，项目成功后发起人需按档位发放回报。
 * 对应数据库表：support_order
 * </p>
 */
@Data
@TableName("support_order")
public class SupportOrder {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号，系统生成的唯一订单号 */
    private String orderNo;

    /** 支持者用户ID，关联sys_user表 */
    private Long userId;

    /** 支持的项目ID */
    private Long projectId;

    /** 支持金额（单位：元） */
    private BigDecimal amount;

    /** 支持留言，用户对项目发起人的留言 */
    private String message;

    /**
     * 订单状态：
     * 0-待支付（订单已创建，等待付款）
     * 1-已支付（付款成功）
     * 2-已取消（用户取消或超时未支付）
     * 3-已退款（项目失败或申请退款后退还）
     */
    private Integer status;

    /** 支付渠道，如微信支付、支付宝等 */
    private String payChannel;

    /** 支付完成时间 */
    private LocalDateTime payTime;

    /**
     * 发货状态：0-未发货，1-已发货
     */
    private Integer deliveryStatus;

    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 物流单号
     */
    private String expressNo;

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
