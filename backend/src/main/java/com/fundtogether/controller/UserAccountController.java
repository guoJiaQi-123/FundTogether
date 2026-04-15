package com.fundtogether.controller;

import com.fundtogether.common.Result;
import com.fundtogether.entity.SysUser;
import com.fundtogether.security.LoginUser;
import com.fundtogether.service.SysUserService;
import com.fundtogether.service.UserRechargeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/account")
public class UserAccountController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private UserRechargeOrderService userRechargeOrderService;

    @GetMapping
    public Result<BigDecimal> getBalance() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getId();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        BigDecimal balance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        return Result.success(balance);
    }

    @PostMapping("/recharge")
    public Result<String> recharge(@RequestBody Map<String, BigDecimal> params) {
        BigDecimal amount = params.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("充值金额必须大于0");
        }
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getId();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        String form = userRechargeOrderService.createRechargeOrder(user.getId(), amount);
        return Result.success(form);
    }

    @PostMapping("/recharge/mock")
    public Result<String> mockRecharge(@RequestBody Map<String, BigDecimal> params) {
        BigDecimal amount = params.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("充值金额必须大于0");
        }
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getId();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 模拟直接增加余额
        BigDecimal currentBalance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        user.setBalance(currentBalance.add(amount));
        sysUserService.updateById(user);

        return Result.success("模拟充值成功");
    }

    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        return userRechargeOrderService.handleAlipayNotify(params);
    }

    @GetMapping("/alipay/return")
    public RedirectView alipayReturn(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        userRechargeOrderService.handleAlipayReturn(params);

        // 重定向回前端账户页面
        return new RedirectView("http://localhost:5173/user/account");
    }
}