package com.fundtogether.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fundtogether.common.Result;
import com.fundtogether.entity.WithdrawalOrder;
import com.fundtogether.security.LoginUser;
import com.fundtogether.service.WithdrawalOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 提现控制器
 * <p>
 * 提供用户提现相关的API功能，包括：
 * <ul>
 *   <li>申请提现：用户提交提现申请，从账户余额中提现到绑定的支付账户</li>
 *   <li>查询我的提现：分页查询当前用户的提现记录</li>
 *   <li>管理员查询所有提现：管理员分页查询所有用户的提现申请</li>
 *   <li>管理员审批提现：管理员审批通过提现申请</li>
 *   <li>管理员驳回提现：管理员驳回提现申请，需提供驳回原因</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/withdrawal")
public class WithdrawalController {

    /** 提现订单服务，处理提现申请、审批及查询 */
    @Autowired
    private WithdrawalOrderService withdrawalOrderService;

    /**
     * 获取当前登录用户ID
     *
     * @return 当前登录用户的ID
     */
    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getId();
    }

    /**
     * 申请提现
     * <p>
     * 用户提交提现申请，从账户余额中提现指定金额到绑定的支付账户。
     * </p>
     *
     * @param params 请求体，包含amount（提现金额）、type（提现方式）、
     *               accountName（账户名）、accountNo（账号）、bankName（银行名称）
     * @return 提现申请结果提示
     */
    @PostMapping("/apply")
    public Result<?> applyWithdrawal(@RequestBody Map<String, Object> params) {
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        Integer type = Integer.parseInt(params.get("type").toString());
        String accountName = (String) params.get("accountName");
        String accountNo = (String) params.get("accountNo");
        String bankName = (String) params.get("bankName");

        withdrawalOrderService.applyWithdrawal(getCurrentUserId(), amount, type, accountName, accountNo, bankName);
        return Result.success("提现申请已提交");
    }

    /**
     * 分页查询当前用户的提现记录
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @return 分页提现订单列表
     */
    @GetMapping("/my-list")
    public Result<IPage<WithdrawalOrder>> getMyWithdrawals(@RequestParam(defaultValue = "1") Integer current,
                                                            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(withdrawalOrderService.getMyWithdrawals(getCurrentUserId(), current, size));
    }

    /**
     * 管理员分页查询所有提现申请（仅管理员）
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @return 分页提现订单列表
     */
    @GetMapping("/admin/list")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<IPage<WithdrawalOrder>> getAllWithdrawals(@RequestParam(defaultValue = "1") Integer current,
                                                             @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(withdrawalOrderService.getAllWithdrawals(current, size));
    }

    /**
     * 管理员审批通过提现申请（仅管理员）
     *
     * @param id 提现订单ID
     * @return 审批结果提示
     */
    @PutMapping("/admin/approve/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> approveWithdrawal(@PathVariable Long id) {
        LoginUser admin = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        withdrawalOrderService.approveWithdrawal(id, admin.getId());
        return Result.success("提现已审批通过");
    }

    /**
     * 管理员驳回提现申请（仅管理员）
     *
     * @param id     提现订单ID
     * @param params 请求体，包含reason（驳回原因）
     * @return 驳回结果提示
     */
    @PutMapping("/admin/reject/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> rejectWithdrawal(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String reason = params.get("reason");
        LoginUser admin = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        withdrawalOrderService.rejectWithdrawal(id, admin.getId(), reason);
        return Result.success("提现已驳回");
    }
}
