package com.fundtogether.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.common.enums.LedgerType;
import com.fundtogether.config.AlipayConfig;
import com.fundtogether.entity.FundingLedger;
import com.fundtogether.entity.SysUser;
import com.fundtogether.entity.UserRechargeOrder;
import com.fundtogether.mapper.SysUserMapper;
import com.fundtogether.mapper.UserRechargeOrderMapper;
import com.fundtogether.service.FundingLedgerService;
import com.fundtogether.service.UserRechargeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class UserRechargeOrderServiceImpl extends ServiceImpl<UserRechargeOrderMapper, UserRechargeOrder> implements UserRechargeOrderService {

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private FundingLedgerService fundingLedgerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createRechargeOrder(Long userId, BigDecimal amount) {
        String orderNo = "RCG" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4);
        
        UserRechargeOrder order = new UserRechargeOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setAmount(amount);
        order.setStatus(0); // 待支付
        this.save(order);

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(AlipayConfig.NOTIFY_URL);
        request.setReturnUrl(AlipayConfig.RETURN_URL);
        
        String bizContent = "{\"out_trade_no\":\"" + orderNo + "\","
                + "\"total_amount\":\"" + amount.toPlainString() + "\","
                + "\"subject\":\"" + "用户充值" + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}";
        request.setBizContent(bizContent);

        try {
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("调用支付宝接口异常", e);
            throw new RuntimeException("生成支付链接失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleAlipayNotify(Map<String, String> params) {
        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, AlipayConfig.SIGN_TYPE);
        } catch (AlipayApiException e) {
            log.error("支付宝验签异常", e);
            return "failure";
        }

        if (!signVerified) {
            log.error("支付宝回调验签失败");
            return "failure";
        }

        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            UserRechargeOrder order = this.lambdaQuery().eq(UserRechargeOrder::getOrderNo, outTradeNo).one();
            if (order == null) {
                log.error("充值订单不存在: {}", outTradeNo);
                return "failure";
            }
            if (order.getStatus() == 1) {
                // 已处理过
                return "success";
            }

            // 更新订单状态
            order.setStatus(1);
            order.setPayTime(LocalDateTime.now());
            this.updateById(order);

            // 更新用户余额
            SysUser user = sysUserMapper.selectById(order.getUserId());
            if (user != null) {
                if (user.getBalance() == null) {
                    user.setBalance(BigDecimal.ZERO);
                }
                user.setBalance(user.getBalance().add(order.getAmount()));
                sysUserMapper.updateById(user);

                FundingLedger ledger = new FundingLedger();
                ledger.setUserId(user.getId());
                ledger.setOrderId(order.getId());
                ledger.setAmount(order.getAmount());
                ledger.setType(LedgerType.USER_RECHARGE.getCode());
                ledger.setStatus(1);
                ledger.setBalanceAfter(user.getBalance());
                ledger.setRemark(String.format("业务场景: 用户充值, 资金流向: 支付宝 -> 用户[%s]账户", user.getNickname()));
                fundingLedgerService.save(ledger);
            }
        }
        return "success";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleAlipayReturn(Map<String, String> params) {
        boolean signVerified;
        try {
            // Note: Alipay return uses AlipaySignature.rsaCheckV1 for RSA2 too (Alipay public key, charset, sign_type)
            signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, AlipayConfig.SIGN_TYPE);
        } catch (AlipayApiException e) {
            log.error("支付宝同步回调验签异常", e);
            return "failure";
        }

        if (!signVerified) {
            log.error("支付宝同步回调验签失败");
            return "failure";
        }

        String outTradeNo = params.get("out_trade_no");

        UserRechargeOrder order = this.lambdaQuery().eq(UserRechargeOrder::getOrderNo, outTradeNo).one();
        if (order == null) {
            log.error("充值订单不存在: {}", outTradeNo);
            return "failure";
        }
        if (order.getStatus() == 1) {
            // 已处理过
            return "success";
        }

        // 更新订单状态
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        this.updateById(order);

        // 更新用户余额
        SysUser user = sysUserMapper.selectById(order.getUserId());
        if (user != null) {
            if (user.getBalance() == null) {
                user.setBalance(BigDecimal.ZERO);
            }
            user.setBalance(user.getBalance().add(order.getAmount()));
            sysUserMapper.updateById(user);

            FundingLedger ledger = new FundingLedger();
            ledger.setUserId(user.getId());
            ledger.setOrderId(order.getId());
            ledger.setAmount(order.getAmount());
            ledger.setType(LedgerType.USER_RECHARGE.getCode());
            ledger.setStatus(1);
            ledger.setBalanceAfter(user.getBalance());
            ledger.setRemark(String.format("业务场景: 用户充值, 资金流向: 支付宝 -> 用户[%s]账户", user.getNickname()));
            fundingLedgerService.save(ledger);
        }
        
        return "success";
    }
}