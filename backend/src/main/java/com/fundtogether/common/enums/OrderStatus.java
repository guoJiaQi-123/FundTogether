package com.fundtogether.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举。
 * <p>
 * 定义众筹订单从创建到终态的完整生命周期状态，用于订单流转控制和状态展示。
 * </p>
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {

    /** 待支付：订单已创建，等待用户完成支付 */
    PENDING(0, "待支付"),

    /** 已支付：用户已完成支付，资金已到账 */
    PAID(1, "已支付"),

    /** 已取消：用户主动取消或超时未支付，订单作废 */
    CANCELED(2, "已取消"),

    /** 已退款：平台将已支付金额退还给用户 */
    REFUNDED(3, "已退款");

    /** 状态编码，存储到数据库中的数值标识 */
    private final int code;

    /** 状态描述，用于前端展示和日志记录 */
    private final String desc;

    /**
     * 根据编码获取对应的订单状态枚举。
     *
     * @param code 订单状态编码
     * @return 对应的订单状态枚举
     * @throws IllegalArgumentException 若编码不存在对应的枚举值
     */
    public static OrderStatus fromCode(int code) {
        for (OrderStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown OrderStatus code: " + code);
    }
}
