package com.fundtogether.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支持者视图对象
 * <p>
 * 用于展示项目的支持者（出资人）信息，包含支持者的个人资料、
 * 支持金额、留言以及发货状态等。通常在项目详情页的支持者列表中使用。
 * </p>
 */
@Data
public class SupporterVO {
    /** 支持订单ID */
    private Long id;
    /** 支持者用户ID */
    private Long userId;
    /** 支持者昵称 */
    private String nickname;
    /** 支持者头像URL */
    private String avatar;
    /** 支持金额 */
    private BigDecimal amount;
    /** 支持者留言 */
    private String message;
    /** 支付时间 */
    private LocalDateTime payTime;

    /** 发货状态（0-未发货, 1-已发货） */
    private Integer deliveryStatus;
    /** 发货时间 */
    private LocalDateTime deliveryTime;
    /** 快递单号 */
    private String expressNo;
}
