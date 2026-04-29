package com.fundtogether.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 台账类型枚举。
 * <p>
 * 定义系统中资金流水（台账）的类型，用于区分不同来源和用途的资金变动记录。
 * </p>
 */
@Getter
@AllArgsConstructor
public enum LedgerType {

    /** 用户支付：用户对众筹项目的支付行为 */
    USER_PAYMENT(1, "用户支付"),

    /** 平台退款：平台向用户退还资金 */
    PLATFORM_REFUND(2, "平台退款"),

    /** 阶段拨付给发起人：项目达到里程碑后，平台将资金拨付给项目发起人 */
    SPONSOR_PAYOUT(3, "阶段拨付给发起人"),

    /** 发起人提现：发起人将账户余额提现至外部账户 */
    SPONSOR_WITHDRAWAL(4, "发起人提现"),

    /** 用户充值：用户通过支付宝或模拟方式向账户充值 */
    USER_RECHARGE(5, "用户充值");

    /** 类型编码，存储到数据库中的数值标识 */
    private final int code;

    /** 类型描述，用于前端展示和日志记录 */
    private final String desc;

    /**
     * 根据编码获取对应的台账类型枚举。
     *
     * @param code 台账类型编码
     * @return 对应的台账类型枚举
     * @throws IllegalArgumentException 若编码不存在对应的枚举值
     */
    public static LedgerType fromCode(int code) {
        for (LedgerType t : values()) {
            if (t.code == code) return t;
        }
        throw new IllegalArgumentException("Unknown LedgerType code: " + code);
    }
}
