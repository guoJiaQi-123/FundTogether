package com.fundtogether.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户实名认证信息DTO（用于认证申请接口）
 * <p>
 * 与UserAuthInfo功能类似，但此DTO专门用于用户发起实名认证申请的接口。
 * 包含真实姓名、身份证号及身份证正反面照片等信息，
 * 提交后由系统或管理员进行审核。
 * </p>
 */
@Data
public class UserAuthInfoDTO {
    /** 用户真实姓名，须与身份证上的姓名一致 */
    @NotBlank(message = "真实姓名不能为空") // 校验规则：真实姓名为必填项，不允许为null、空字符串或纯空白
    private String realName;

    /** 身份证号码，须为18位有效身份证号 */
    @NotBlank(message = "身份证号不能为空") // 校验规则：身份证号为必填项，不允许为null、空字符串或纯空白
    private String idCard;

    /** 身份证正面（人像面）照片URL地址 */
    @NotBlank(message = "身份证正面不能为空") // 校验规则：身份证正面照片为必填项，不允许为null、空字符串或纯空白
    private String idCardFront;

    /** 身份证反面（国徽面）照片URL地址 */
    @NotBlank(message = "身份证反面不能为空") // 校验规则：身份证反面照片为必填项，不允许为null、空字符串或纯空白
    private String idCardBack;
}
