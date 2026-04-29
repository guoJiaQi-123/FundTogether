package com.fundtogether.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户重置密码的数据传输对象
 * <p>
 * 用于用户忘记密码时通过验证码方式重置密码。
 * 用户需提供注册账号、验证码和新密码，
 * 系统验证验证码正确后即可设置新密码，无需输入原密码。
 * </p>
 */
@Data
public class UserResetPasswordDTO {
    /** 注册账号，可以是手机号或邮箱，用于定位要重置密码的用户 */
    @NotBlank(message = "账号不能为空") // 校验规则：账号为必填项，不允许为null、空字符串或纯空白
    private String account;

    /** 短信/邮件验证码，用于验证用户身份的真实性 */
    @NotBlank(message = "验证码不能为空") // 校验规则：验证码为必填项，不允许为null、空字符串或纯空白
    private String code;

    /** 新密码，重置后用户使用此密码登录 */
    @NotBlank(message = "新密码不能为空") // 校验规则：新密码为必填项，不允许为null、空字符串或纯空白
    private String newPassword;
}
