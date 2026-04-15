package com.fundtogether.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
    USER(1, "普通用户"),
    SPONSOR(2, "项目发起人"),
    ADMIN(4, "管理员");

    private final int code;
    private final String desc;

    public static boolean hasRole(int roleMask, UserRole role) {
        return (roleMask & role.code) != 0;
    }

    public static boolean isAdmin(int roleMask) {
        return hasRole(roleMask, ADMIN);
    }

    public static boolean isSponsor(int roleMask) {
        return hasRole(roleMask, SPONSOR);
    }

    public static boolean isUser(int roleMask) {
        return hasRole(roleMask, USER);
    }

    public static int combine(UserRole... roles) {
        int mask = 0;
        for (UserRole r : roles) {
            mask |= r.code;
        }
        return mask;
    }
}
