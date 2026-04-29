package com.fundtogether.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建支持订单（众筹付款）的数据传输对象
 * <p>
 * 用于用户对众筹项目发起支持（付款）时提交的数据。
 * 包含支持的项目、金额、留言以及支付渠道等信息。
 * 创建后生成一条支持订单记录，需完成支付后才算支持成功。
 * </p>
 */
@Data
public class SupportOrderCreateDTO {

    /** 支持的项目ID，标识用户要支持哪个众筹项目 */
    @NotNull(message = "项目ID不能为空") // 校验规则：项目ID为必填项，不允许为null
    private Long projectId;

    /** 支持金额（单位：元），用户愿意为该项目支付的金额 */
    @NotNull(message = "支持金额不能为空") // 校验规则：支持金额为必填项，不允许为null
    @DecimalMin(value = "0.01", message = "支持金额必须大于0") // 校验规则：金额最小值为0.01元，确保金额为正数
    private BigDecimal amount;

    /** 支持留言，用户对项目发起人的留言，可选字段 */
    private String message;

    /** 支付渠道标识，如微信支付、支付宝等，可选字段 */
    private String payChannel;
}
