package com.fundtogether.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fundtogether.common.Result;
import com.fundtogether.common.enums.LedgerType;
import com.fundtogether.entity.FundingLedger;
import com.fundtogether.entity.Project;
import com.fundtogether.entity.SysUser;
import com.fundtogether.entity.UserRechargeOrder;
import com.fundtogether.security.LoginUser;
import com.fundtogether.service.FundingLedgerService;
import com.fundtogether.service.ProjectService;
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

/**
 * 用户账户控制器
 * <p>
 * 提供用户账户资金相关的API功能，包括：
 * <ul>
 * <li>查询余额：获取当前用户的账户余额</li>
 * <li>充值（支付宝）：创建支付宝充值订单，返回支付表单</li>
 * <li>模拟充值：开发环境下的模拟充值，直接增加余额</li>
 * <li>支付宝异步通知：接收支付宝支付结果的异步回调</li>
 * <li>支付宝同步回调：支付完成后重定向回前端页面</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/user/account")
public class UserAccountController {

    /** 用户服务，用于查询用户信息及更新余额 */
    @Autowired
    private SysUserService sysUserService;

    /** 用户充值订单服务，处理充值订单创建及支付宝回调 */
    @Autowired
    private UserRechargeOrderService userRechargeOrderService;

    /** 资金台账服务，用于记录充值流水 */
    @Autowired
    private FundingLedgerService fundingLedgerService;

    /** 项目服务，用于补充项目名称 */
    @Autowired
    private ProjectService projectService;

    /**
     * 查询当前用户账户余额
     *
     * @return 用户账户余额，若余额为null则返回0
     */
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

    /**
     * 发起支付宝充值
     * <p>
     * 创建充值订单并生成支付宝支付表单HTML，前端提交表单后跳转到支付宝支付页面。
     * </p>
     *
     * @param params 请求体，包含amount（充值金额）
     * @return 支付宝支付表单HTML字符串
     */
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

        // 创建充值订单并获取支付宝支付表单
        String form = userRechargeOrderService.createRechargeOrder(user.getId(), amount);
        return Result.success(form);
    }

    /**
     * 模拟充值（开发环境使用）
     * <p>
     * 跳过支付宝支付流程，直接增加用户余额，仅用于开发测试环境。
     * </p>
     *
     * @param params 请求体，包含amount（充值金额）
     * @return 模拟充值结果提示
     */
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

        FundingLedger ledger = new FundingLedger();
        ledger.setUserId(user.getId());
        ledger.setAmount(amount);
        ledger.setType(LedgerType.USER_RECHARGE.getCode());
        ledger.setStatus(1);
        ledger.setBalanceAfter(user.getBalance());
        ledger.setRemark(String.format("业务场景: 模拟充值, 资金流向: 模拟 -> 用户[%s]账户", user.getNickname()));
        fundingLedgerService.save(ledger);

        return Result.success("模拟充值成功");
    }

    /**
     * 查询当前用户的充值记录（分页）
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @return 充值订单分页数据
     */
    @GetMapping("/recharge/my-list")
    public Result<IPage<UserRechargeOrder>> getMyRechargeOrders(@RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getId();
        IPage<UserRechargeOrder> page = userRechargeOrderService.lambdaQuery()
                .eq(UserRechargeOrder::getUserId, userId)
                .orderByDesc(UserRechargeOrder::getCreatedAt)
                .page(new Page<>(current, size));
        return Result.success(page);
    }

    /**
     * 查询当前用户的资金明细（分页）
     * <p>
     * 返回当前用户的所有资金流水记录，包含每笔交易后的账户余额。
     * 支持按流水类型筛选（1-用户支付，2-平台退款，3-阶段拨付，4-发起人提现，5-用户充值）。
     * </p>
     *
     * @param type    流水类型，可选，用于筛选指定类型的流水
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @return 资金流水分页数据（含项目名称和交易后余额）
     */
    @GetMapping("/transactions")
    public Result<IPage<FundingLedger>> getMyTransactions(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getId();

        LambdaQueryWrapper<FundingLedger> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundingLedger::getUserId, userId);
        if (type != null) {
            wrapper.eq(FundingLedger::getType, type);
        }
        wrapper.orderByDesc(FundingLedger::getCreatedAt);

        Page<FundingLedger> page = new Page<>(current, size);
        IPage<FundingLedger> result = fundingLedgerService.page(page, wrapper);

        for (FundingLedger ledger : result.getRecords()) {
            if (ledger.getProjectId() != null) {
                Project project = projectService.getById(ledger.getProjectId());
                if (project != null) {
                    ledger.setProjectName(project.getTitle());
                }
            }
        }

        return Result.success(result);
    }

    /**
     * 支付宝异步通知回调
     * <p>
     * 接收支付宝支付结果的异步通知，验证签名后更新充值订单状态和用户余额。
     * </p>
     *
     * @param request HTTP请求对象，包含支付宝回调参数
     * @return 处理结果字符串（"success"或"failure"）
     */
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

    /**
     * 支付宝同步回调（前端跳转）
     * <p>
     * 用户在支付宝完成支付后，支付宝同步回调此接口，
     * 处理回调参数后重定向回前端用户账户页面。
     * </p>
     *
     * @param request HTTP请求对象，包含支付宝回调参数
     * @return 重定向到前端账户页面的RedirectView
     */
    @GetMapping("/alipay/return")
    public RedirectView alipayReturn(HttpServletRequest request) {
        // 解析支付宝回调的所有参数
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
        return new RedirectView("http://localhost:5173/user/profile?tab=account");
    }
}
