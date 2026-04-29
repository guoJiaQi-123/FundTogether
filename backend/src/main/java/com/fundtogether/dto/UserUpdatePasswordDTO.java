package com.fundtogether.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户修改密码的数据传输对象
 * <p>
 * 用于已登录用户在知道原密码的情况下主动修改密码。
 * 需同时提供原密码和新密码，系统验证原密码正确后方可修改。
 * 与UserResetPasswordDTO不同，此DTO适用于用户主动修改密码的场景。
 * </p>
 */
@Data
public class UserUpdatePasswordDTO {
    /** 原密码，用于身份验证，确认是本人操作 */
    @NotBlank(message = "原密码不能为空") // 校验规则：原密码为必填项，不允许为null、空字符串或纯空白
    private String oldPassword;

    /** 新密码，修改后用户使用此密码登录 */
    @NotBlank(message = "新密码不能为空") // 校验规则：新密码为必填项，不允许为null、空字符串或纯空白
    private String newPassword;
}
