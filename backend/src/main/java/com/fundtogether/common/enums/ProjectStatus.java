package com.fundtogether.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectStatus {
    PENDING_REVIEW(0, "待审核"),
    FUNDING(1, "筹款中"),
    REJECTED(2, "已驳回"),
    CANCELED(3, "已取消"),
    OFFLINE(4, "已下架"),
    SUCCESS(5, "已完成"),
    FAILED(6, "筹款失败");

    private final int code;
    private final String desc;

    public static ProjectStatus fromCode(int code) {
        for (ProjectStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown ProjectStatus code: " + code);
    }
}
