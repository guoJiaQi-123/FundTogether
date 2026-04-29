package com.fundtogether.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举。
 * <p>
 * 定义系统中站内消息的分类类型，用于消息的归类、筛选和展示。
 * </p>
 */
@Getter
@AllArgsConstructor
public enum MessageType {

    /** 系统公告：平台发布的全局通知 */
    SYSTEM_NOTICE(1, "系统公告"),

    /** 项目动态：用户关注的项目状态变更通知 */
    PROJECT_UPDATE(2, "项目动态"),

    /** 评论回复：其他用户对当前用户评论的回复通知 */
    COMMENT_REPLY(3, "评论回复"),

    /** 订单及状态通知：订单支付、退款等状态变更通知 */
    ORDER_STATUS(4, "订单及状态通知");

    /** 类型编码，存储到数据库中的数值标识 */
    private final int code;

    /** 类型描述，用于前端展示和日志记录 */
    private final String desc;

    /**
     * 根据编码获取对应的消息类型枚举。
     *
     * @param code 消息类型编码
     * @return 对应的消息类型枚举
     * @throws IllegalArgumentException 若编码不存在对应的枚举值
     */
    public static MessageType fromCode(int code) {
        for (MessageType t : values()) {
            if (t.code == code) return t;
        }
        throw new IllegalArgumentException("Unknown MessageType code: " + code);
    }
}
