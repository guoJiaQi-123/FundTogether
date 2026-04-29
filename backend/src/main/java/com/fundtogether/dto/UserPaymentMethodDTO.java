package com.fundtogether.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户支付方式的数据传输对象
 * <p>
 * 用于用户添加或修改提现/收款账户信息时提交的数据。
 * 支持微信、支付宝和银行卡三种支付类型，
 * 不同类型需要填写不同的账户信息。
 * </p>
 */
@Data
public class UserPaymentMethodDTO {
    /** 支付类型：1-微信，2-支付宝，3-银行卡 */
    @NotNull(message = "支付类型不能为空") // 校验规则：支付类型为必填项，不允许为null
    private Integer type;

    /** 支付账号，微信/支付宝为对应账号，银行卡为卡号 */
    @NotBlank(message = "账号不能为空") // 校验规则：支付账号为必填项，不允许为null、空字符串或纯空白
    private String account;

    /** 持卡人/账户姓名，银行卡类型时为持卡人姓名 */
    private String name;

    /** 开户银行名称，仅银行卡类型时需要填写 */
    private String bankName;
}
