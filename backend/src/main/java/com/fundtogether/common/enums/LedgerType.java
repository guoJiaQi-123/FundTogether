package com.fundtogether.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LedgerType {
    USER_PAYMENT(1, "用户支付"),
    PLATFORM_REFUND(2, "平台退款"),
    SPONSOR_PAYOUT(3, "阶段拨付给发起人");

    private final int code;
    private final String desc;

    public static LedgerType fromCode(int code) {
        for (LedgerType t : values()) {
            if (t.code == code) return t;
        }
        throw new IllegalArgumentException("Unknown LedgerType code: " + code);
    }
}
