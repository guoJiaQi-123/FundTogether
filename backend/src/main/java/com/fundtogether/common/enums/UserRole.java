package com.fundtogether.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举。
 * <p>
 * 定义系统中的用户角色类型，采用位掩码（bitmask）设计，
 * 支持一个用户同时拥有多种角色（如既是普通用户又是项目发起人）。
 * 角色编码为 2 的幂次：USER=1(2⁰), SPONSOR=2(2¹), ADMIN=4(2²)。
 * </p>
 */
@Getter
@AllArgsConstructor
public enum UserRole {

    /** 普通用户：可以浏览项目、参与出资等基础操作 */
    USER(1, "普通用户"),

    /** 项目发起人：可以创建和管理众筹项目 */
    SPONSOR(2, "项目发起人"),

    /** 管理员：拥有系统管理权限，可审核项目、管理用户等 */
    ADMIN(4, "管理员");

    /** 角色编码，为 2 的幂次，用于位掩码运算 */
    private final int code;

    /** 角色描述，用于前端展示和日志记录 */
    private final String desc;

    /**
     * 判断角色掩码中是否包含指定角色。
     * <p>
     * 使用按位与运算检查掩码中是否包含目标角色的位。
     * </p>
     *
     * @param roleMask 用户角色掩码，多个角色通过位或运算组合
     * @param role     待检查的目标角色
     * @return 若掩码中包含该角色则返回 true，否则返回 false
     */
    public static boolean hasRole(int roleMask, UserRole role) {
        return (roleMask & role.code) != 0;
    }

    /**
     * 判断角色掩码中是否包含管理员角色。
     *
     * @param roleMask 用户角色掩码
     * @return 若为管理员则返回 true，否则返回 false
     */
    public static boolean isAdmin(int roleMask) {
        return hasRole(roleMask, ADMIN);
    }

    /**
     * 判断角色掩码中是否包含项目发起人角色。
     *
     * @param roleMask 用户角色掩码
     * @return 若为项目发起人则返回 true，否则返回 false
     */
    public static boolean isSponsor(int roleMask) {
        return hasRole(roleMask, SPONSOR);
    }

    /**
     * 判断角色掩码中是否包含普通用户角色。
     *
     * @param roleMask 用户角色掩码
     * @return 若为普通用户则返回 true，否则返回 false
     */
    public static boolean isUser(int roleMask) {
        return hasRole(roleMask, USER);
    }

    /**
     * 将多个角色合并为一个角色掩码。
     * <p>
     * 使用按位或运算将多个角色的编码组合为一个整数值，
     * 便于存储到数据库的单个字段中。
     * </p>
     *
     * @param roles 需要合并的角色列表
     * @return 合并后的角色掩码值
     */
    public static int combine(UserRole... roles) {
        int mask = 0;
        for (UserRole r : roles) {
            mask |= r.code;
        }
        return mask;
    }
}
