package com.fundtogether.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fundtogether.common.Result;
import com.fundtogether.common.enums.UserRole;
import com.fundtogether.entity.FundingLedger;
import com.fundtogether.entity.Project;
import com.fundtogether.entity.ProjectPayout;
import com.fundtogether.entity.SysUser;
import com.fundtogether.security.LoginUser;
import com.fundtogether.service.FundingLedgerService;
import com.fundtogether.service.ProjectPayoutService;
import com.fundtogether.service.ProjectService;
import com.fundtogether.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/funding")
public class FundingController {

    @Autowired
    private ProjectPayoutService projectPayoutService;
    
    @Autowired
    private FundingLedgerService fundingLedgerService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SysUserService sysUserService;

    private LoginUser getCurrentUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // List payouts for a project
    @GetMapping("/payouts")
    public Result<IPage<ProjectPayout>> getPayouts(@RequestParam(required = false) Long projectId,
                                                   @RequestParam(defaultValue = "1") Integer current,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        LoginUser user = getCurrentUser();
        LambdaQueryWrapper<ProjectPayout> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(ProjectPayout::getProjectId, projectId);
        }
        // If not admin, can only see their own projects
        if (!UserRole.isAdmin(user.getRole())) {
            wrapper.eq(ProjectPayout::getSponsorId, user.getId());
        }
        wrapper.orderByAsc(ProjectPayout::getStage);
        
        Page<ProjectPayout> page = new Page<>(current, size);
        IPage<ProjectPayout> result = projectPayoutService.page(page, wrapper);
        for (ProjectPayout payout : result.getRecords()) {
            Project project = projectService.getById(payout.getProjectId());
            if (project != null) {
                payout.setProjectName(project.getTitle());
            }
            SysUser sponsor = sysUserService.getById(payout.getSponsorId());
            if (sponsor != null) {
                payout.setSponsorName(sponsor.getNickname());
            }
        }
        return Result.success(result);
    }

    // Process a payout (Admin only)
    @PostMapping("/process-payout/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> processPayout(@PathVariable Long id) {
        ProjectPayout payout = projectPayoutService.getById(id);
        if (payout == null) {
            return Result.error("拨付记录不存在");
        }
        if (payout.getStatus() == 1) {
            return Result.error("该记录已拨付");
        }
        
        // Update payout status
        payout.setStatus(1); // 已拨付
        payout.setPayoutTime(LocalDateTime.now());
        projectPayoutService.updateById(payout);
        
        // Add funds to project sponsor's account balance
        SysUser sponsor = sysUserService.getById(payout.getSponsorId());
        if (sponsor != null) {
            BigDecimal currentBalance = sponsor.getBalance() != null ? sponsor.getBalance() : BigDecimal.ZERO;
            sponsor.setBalance(currentBalance.add(payout.getAmount()));
            sysUserService.updateById(sponsor);
        }

        // Record in ledger
        FundingLedger ledger = new FundingLedger();
        ledger.setProjectId(payout.getProjectId());
        ledger.setUserId(payout.getSponsorId());
        ledger.setAmount(payout.getAmount());
        ledger.setType(3); // 3-阶段拨付给发起人
        ledger.setStatus(1); // 成功
        Project project = projectService.getById(payout.getProjectId());
        String projectName = project != null ? project.getTitle() : "未知项目";
        String sponsorName = sponsor != null ? sponsor.getNickname() : "未知用户";
        ledger.setRemark(String.format("业务场景: 项目阶段拨付[%s], 资金流向: 平台 -> 发起人[%s] -> 项目[%s]", payout.getConditionDesc(), sponsorName, projectName));
        fundingLedgerService.save(ledger);
        
        return Result.success("拨付成功");
    }

    // List transaction ledgers
    @GetMapping("/ledgers")
    public Result<IPage<FundingLedger>> getLedgers(@RequestParam(required = false) Long projectId,
                                                   @RequestParam(defaultValue = "1") Integer current,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        LoginUser user = getCurrentUser();
        LambdaQueryWrapper<FundingLedger> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(FundingLedger::getProjectId, projectId);
        }
        // If not admin, can only see their own transactions (either as supporter or sponsor)
        if (!UserRole.isAdmin(user.getRole())) {
            wrapper.eq(FundingLedger::getUserId, user.getId());
        }
        wrapper.orderByDesc(FundingLedger::getCreatedAt);
        
        Page<FundingLedger> page = new Page<>(current, size);
        IPage<FundingLedger> result = fundingLedgerService.page(page, wrapper);
        for (FundingLedger ledger : result.getRecords()) {
            Project project = projectService.getById(ledger.getProjectId());
            if (project != null) {
                ledger.setProjectName(project.getTitle());
            }
            SysUser u = sysUserService.getById(ledger.getUserId());
            if (u != null) {
                ledger.setUserName(u.getNickname());
            }
        }
        return Result.success(result);
    }

    // Get Platform Account Overview
    @GetMapping("/platform-account")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> getPlatformAccount() {
        // compute incoming (type=1, status=1)
        LambdaQueryWrapper<FundingLedger> inWrapper = new LambdaQueryWrapper<>();
        inWrapper.eq(FundingLedger::getType, 1).eq(FundingLedger::getStatus, 1);
        List<FundingLedger> inList = fundingLedgerService.list(inWrapper);
        BigDecimal totalIncoming = inList.stream()
            .map(FundingLedger::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // compute outgoing (type=2 or type=3, status=1)
        LambdaQueryWrapper<FundingLedger> outWrapper = new LambdaQueryWrapper<>();
        outWrapper.in(FundingLedger::getType, Arrays.asList(2, 3)).eq(FundingLedger::getStatus, 1);
        List<FundingLedger> outList = fundingLedgerService.list(outWrapper);
        BigDecimal totalOutgoing = outList.stream()
            .map(FundingLedger::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncoming.subtract(totalOutgoing);

        Map<String, Object> res = new HashMap<>();
        res.put("totalIncoming", totalIncoming);
        res.put("totalOutgoing", totalOutgoing);
        res.put("balance", balance);

        return Result.success(res);
    }
}
