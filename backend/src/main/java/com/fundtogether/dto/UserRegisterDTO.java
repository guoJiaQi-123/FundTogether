package com.fundtogether.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户注册的数据传输对象
 * <p>
 * 用于新用户注册账号时提交的数据。
 * 支持手机号和邮箱两种注册方式，通过registerType字段区分。
 * 注册时需要输入验证码进行身份验证，并需确认密码。
 * </p>
 */
@Data
public class UserRegisterDTO {

    /** 注册方式：phone-手机号注册，email-邮箱注册 */
    @NotBlank(message = "注册方式不能为空") // 校验规则：注册方式为必填项，不允许为null、空字符串或纯空白
    @Pattern(regexp = "^(phone|email)$", message = "注册方式只能是手机号(phone)或邮箱(email)") // 校验规则：仅允许phone或email两个值
    private String registerType;

    /** 注册账号，根据registerType为手机号或邮箱地址 */
    @NotBlank(message = "账号不能为空") // 校验规则：注册账号为必填项，不允许为null、空字符串或纯空白
    private String account;

    /** 登录密码 */
    @NotBlank(message = "密码不能为空") // 校验规则：密码为必填项，不允许为null、空字符串或纯空白
    private String password;

    /** 确认密码，须与password字段一致 */
    @NotBlank(message = "确认密码不能为空") // 校验规则：确认密码为必填项，不允许为null、空字符串或纯空白
    private String confirmPassword;

    /** 短信/邮件验证码，用于验证手机号或邮箱的真实性 */
    @NotBlank(message = "验证码不能为空") // 校验规则：验证码为必填项，不允许为null、空字符串或纯空白
    private String code;
}
