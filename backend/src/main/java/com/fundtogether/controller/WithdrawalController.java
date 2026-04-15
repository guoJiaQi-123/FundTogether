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

@RestController
@RequestMapping("/api/withdrawal")
public class WithdrawalController {

    @Autowired
    private WithdrawalOrderService withdrawalOrderService;

    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getId();
    }

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

    @GetMapping("/my-list")
    public Result<IPage<WithdrawalOrder>> getMyWithdrawals(@RequestParam(defaultValue = "1") Integer current,
                                                            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(withdrawalOrderService.getMyWithdrawals(getCurrentUserId(), current, size));
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<IPage<WithdrawalOrder>> getAllWithdrawals(@RequestParam(defaultValue = "1") Integer current,
                                                             @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(withdrawalOrderService.getAllWithdrawals(current, size));
    }

    @PutMapping("/admin/approve/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> approveWithdrawal(@PathVariable Long id) {
        LoginUser admin = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        withdrawalOrderService.approveWithdrawal(id, admin.getId());
        return Result.success("提现已审批通过");
    }

    @PutMapping("/admin/reject/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> rejectWithdrawal(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String reason = params.get("reason");
        LoginUser admin = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        withdrawalOrderService.rejectWithdrawal(id, admin.getId(), reason);
        return Result.success("提现已驳回");
    }
}
