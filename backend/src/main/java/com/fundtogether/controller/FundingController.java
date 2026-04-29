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

/**
 * 资金管理控制器
 * <p>
 * 提供众筹平台资金流转相关的API功能，包括：
 * <ul>
 *   <li>拨付记录管理：查询项目阶段拨付记录、管理员执行拨付操作</li>
 *   <li>资金台账查询：分页查询资金流水记录，普通用户仅可查看自己的记录</li>
 *   <li>平台账户总览：管理员查看平台收支汇总（总收入、总支出、余额）</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/funding")
public class FundingController {

    /** 项目拨付服务，处理项目阶段拨付记录的查询与拨付操作 */
    @Autowired
    private ProjectPayoutService projectPayoutService;

    /** 资金台账服务，处理资金流水记录的查询 */
    @Autowired
    private FundingLedgerService fundingLedgerService;

    /** 项目服务，用于查询项目信息补充显示 */
    @Autowired
    private ProjectService projectService;

    /** 用户服务，用于查询用户信息补充显示及拨付时更新余额 */
    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取当前登录用户信息
     *
     * @return 当前登录的LoginUser对象
     */
    private LoginUser getCurrentUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * 分页查询拨付记录列表
     * <p>
     * 管理员可查看所有拨付记录，普通用户仅可查看自己作为发起人的项目的拨付记录。
     * 返回结果中会补充项目名称和发起人昵称。
     * </p>
     *
     * @param projectId 项目ID，可选，用于筛选指定项目的拨付记录
     * @param current   当前页码，默认1
     * @param size      每页条数，默认10
     * @return 分页拨付记录列表（含项目名称和发起人昵称）
     */
    @GetMapping("/payouts")
    public Result<IPage<ProjectPayout>> getPayouts(@RequestParam(required = false) Long projectId,
                                                   @RequestParam(defaultValue = "1") Integer current,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        LoginUser user = getCurrentUser();
        LambdaQueryWrapper<ProjectPayout> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(ProjectPayout::getProjectId, projectId);
        }
        // 非管理员只能查看自己发起的项目的拨付记录
        if (!UserRole.isAdmin(user.getRole())) {
            wrapper.eq(ProjectPayout::getSponsorId, user.getId());
        }
        wrapper.orderByAsc(ProjectPayout::getStage);

        Page<ProjectPayout> page = new Page<>(current, size);
        IPage<ProjectPayout> result = projectPayoutService.page(page, wrapper);
        // 补充项目名称和发起人昵称
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

    /**
     * 执行拨付操作（仅管理员）
     * <p>
     * 管理员确认拨付后，系统会：
     * <ol>
     *   <li>更新拨付记录状态为已拨付</li>
     *   <li>将拨付金额添加到项目发起人的账户余额</li>
     *   <li>在资金台账中记录本次拨付流水</li>
     * </ol>
     * 该操作在事务中执行，任一步骤失败将全部回滚。
     * </p>
     *
     * @param id 拨付记录ID
     * @return 拨付结果提示
     */
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

        // 更新拨付记录状态为已拨付
        payout.setStatus(1); // 已拨付
        payout.setPayoutTime(LocalDateTime.now());
        projectPayoutService.updateById(payout);

        // 将拨付金额添加到项目发起人的账户余额
        SysUser sponsor = sysUserService.getById(payout.getSponsorId());
        if (sponsor != null) {
            BigDecimal currentBalance = sponsor.getBalance() != null ? sponsor.getBalance() : BigDecimal.ZERO;
            sponsor.setBalance(currentBalance.add(payout.getAmount()));
            sysUserService.updateById(sponsor);
        }

        // 在资金台账中记录本次拨付流水
        FundingLedger ledger = new FundingLedger();
        ledger.setProjectId(payout.getProjectId());
        ledger.setUserId(payout.getSponsorId());
        ledger.setAmount(payout.getAmount());
        ledger.setType(3); // 3-阶段拨付给发起人
        ledger.setStatus(1); // 成功
        SysUser sponsorAfter = sysUserService.getById(payout.getSponsorId());
        if (sponsorAfter != null) {
            ledger.setBalanceAfter(sponsorAfter.getBalance() != null ? sponsorAfter.getBalance() : BigDecimal.ZERO);
        }
        Project project = projectService.getById(payout.getProjectId());
        String projectName = project != null ? project.getTitle() : "未知项目";
        String sponsorName = sponsor != null ? sponsor.getNickname() : "未知用户";
        ledger.setRemark(String.format("业务场景: 项目阶段拨付[%s], 资金流向: 平台 -> 发起人[%s] -> 项目[%s]", payout.getConditionDesc(), sponsorName, projectName));
        fundingLedgerService.save(ledger);

        return Result.success("拨付成功");
    }

    /**
     * 分页查询资金台账记录
     * <p>
     * 管理员可查看所有资金流水，普通用户仅可查看与自己相关的流水记录。
     * 返回结果中会补充项目名称和用户昵称。
     * </p>
     *
     * @param projectId 项目ID，可选，用于筛选指定项目的资金流水
     * @param current   当前页码，默认1
     * @param size      每页条数，默认10
     * @return 分页资金台账列表（含项目名称和用户昵称）
     */
    @GetMapping("/ledgers")
    public Result<IPage<FundingLedger>> getLedgers(@RequestParam(required = false) Long projectId,
                                                   @RequestParam(required = false, defaultValue = "false") Boolean mine,
                                                   @RequestParam(required = false) Integer excludeType,
                                                   @RequestParam(defaultValue = "1") Integer current,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        LoginUser user = getCurrentUser();
        LambdaQueryWrapper<FundingLedger> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(FundingLedger::getProjectId, projectId);
        }
        if (mine || !UserRole.isAdmin(user.getRole())) {
            wrapper.eq(FundingLedger::getUserId, user.getId());
        }
        if (excludeType != null) {
            wrapper.ne(FundingLedger::getType, excludeType);
        }
        wrapper.orderByDesc(FundingLedger::getCreatedAt);

        Page<FundingLedger> page = new Page<>(current, size);
        IPage<FundingLedger> result = fundingLedgerService.page(page, wrapper);
        // 补充项目名称和用户昵称
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

    /**
     * 获取平台账户总览（仅管理员）
     * <p>
     * 统计平台的总收入（type=1, status=1）、总支出（type=2或3, status=1）和余额，
     * 余额 = 总收入 - 总支出。
     * </p>
     *
     * @return 平台账户数据Map，包含totalIncoming（总收入）、totalOutgoing（总支出）、balance（余额）
     */
    @GetMapping("/platform-account")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> getPlatformAccount() {
        // 计算总收入：type=1（支持支付）且status=1（成功）的台账记录
        LambdaQueryWrapper<FundingLedger> inWrapper = new LambdaQueryWrapper<>();
        inWrapper.eq(FundingLedger::getType, 1).eq(FundingLedger::getStatus, 1);
        List<FundingLedger> inList = fundingLedgerService.list(inWrapper);
        BigDecimal totalIncoming = inList.stream()
            .map(FundingLedger::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算总支出：type=2（退款）或type=3（拨付）且status=1（成功）的台账记录
        LambdaQueryWrapper<FundingLedger> outWrapper = new LambdaQueryWrapper<>();
        outWrapper.in(FundingLedger::getType, Arrays.asList(2, 3)).eq(FundingLedger::getStatus, 1);
        List<FundingLedger> outList = fundingLedgerService.list(outWrapper);
        BigDecimal totalOutgoing = outList.stream()
            .map(FundingLedger::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 余额 = 总收入 - 总支出
        BigDecimal balance = totalIncoming.subtract(totalOutgoing);

        Map<String, Object> res = new HashMap<>();
        res.put("totalIncoming", totalIncoming);
        res.put("totalOutgoing", totalOutgoing);
        res.put("balance", balance);

        return Result.success(res);
    }
}
