package com.fundtogether.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 项目状态枚举。
 * <p>
 * 定义众筹项目从创建到终态的完整生命周期状态，用于项目审核、上下架和结果判定等流程控制。
 * </p>
 */
@Getter
@AllArgsConstructor
public enum ProjectStatus {

    /** 待审核：项目已提交，等待管理员审核 */
    PENDING_REVIEW(0, "待审核"),

    /** 筹款中：审核通过，项目正在接受用户出资 */
    FUNDING(1, "筹款中"),

    /** 已驳回：审核未通过，项目被拒绝 */
    REJECTED(2, "已驳回"),

    /** 已取消：发起人主动取消项目 */
    CANCELED(3, "已取消"),

    /** 已下架：项目因违规等原因被管理员强制下架 */
    OFFLINE(4, "已下架"),

    /** 已完成：项目筹款成功，资金已拨付 */
    SUCCESS(5, "已完成"),

    /** 筹款失败：项目未在规定时间内达到筹款目标 */
    FAILED(6, "筹款失败");

    /** 状态编码，存储到数据库中的数值标识 */
    private final int code;

    /** 状态描述，用于前端展示和日志记录 */
    private final String desc;

    /**
     * 根据编码获取对应的项目状态枚举。
     *
     * @param code 项目状态编码
     * @return 对应的项目状态枚举
     * @throws IllegalArgumentException 若编码不存在对应的枚举值
     */
    public static ProjectStatus fromCode(int code) {
        for (ProjectStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown ProjectStatus code: " + code);
    }
}
