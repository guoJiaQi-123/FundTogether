package com.fundtogether.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {
    SYSTEM_NOTICE(1, "系统公告"),
    PROJECT_UPDATE(2, "项目动态"),
    COMMENT_REPLY(3, "评论回复"),
    ORDER_STATUS(4, "订单及状态通知");

    private final int code;
    private final String desc;

    public static MessageType fromCode(int code) {
        for (MessageType t : values()) {
            if (t.code == code) return t;
        }
        throw new IllegalArgumentException("Unknown MessageType code: " + code);
    }
}
