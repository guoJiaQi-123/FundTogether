package com.fundtogether.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fundtogether.entity.UserRechargeOrder;
import java.util.Map;

public interface UserRechargeOrderService extends IService<UserRechargeOrder> {

    /**
     * 创建充值订单并返回支付宝支付URL/表单
     */
    String createRechargeOrder(Long userId, java.math.BigDecimal amount);

    /**
     * 处理支付宝异步通知
     */
    String handleAlipayNotify(Map<String, String> params);

    String handleAlipayReturn(Map<String, String> params);
}