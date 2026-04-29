package com.fundtogether.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录的数据传输对象
 * <p>
 * 用于用户通过账号密码方式登录系统时提交的数据。
 * 账号可以是手机号或邮箱，与注册时使用的方式一致。
 * </p>
 */
@Data
public class UserLoginDTO {

    /** 登录账号，可以是手机号或邮箱 */
    @NotBlank(message = "账号不能为空") // 校验规则：登录账号为必填项，不允许为null、空字符串或纯空白
    private String account;

    /** 登录密码 */
    @NotBlank(message = "密码不能为空") // 校验规则：登录密码为必填项，不允许为null、空字符串或纯空白
    private String password;
}
