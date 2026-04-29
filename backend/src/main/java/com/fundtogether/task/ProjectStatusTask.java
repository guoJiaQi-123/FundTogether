package com.fundtogether.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fundtogether.entity.Project;
import com.fundtogether.entity.ProjectPayout;
import com.fundtogether.entity.SupportOrder;
import com.fundtogether.entity.FundingLedger;
import com.fundtogether.service.ProjectService;
import com.fundtogether.service.ProjectPayoutService;
import com.fundtogether.service.SupportOrderService;
import com.fundtogether.service.FundingLedgerService;
import com.fundtogether.service.SysUserService;
import com.fundtogether.service.HeatCalculationService;
import com.fundtogether.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目状态定时任务
 * <p>
 * 定时检查众筹项目的状态，当项目到达截止时间后，根据筹款金额判断项目是否成功：
 * <ul>
 *   <li>筹款成功（当前金额 >= 目标金额）：更新项目状态为"已成功"，并生成分阶段拨付计划</li>
 *   <li>筹款失败（当前金额 < 目标金额）：更新项目状态为"筹款失败"，并自动退款给所有支持者</li>
 * </ul>
 * 该任务每分钟执行一次，通过Spring的@Scheduled注解驱动。
 * </p>
 */
@Slf4j
@Component
public class ProjectStatusTask {

    /** 项目服务，用于查询和更新项目信息 */
    @Autowired
    private ProjectService projectService;

    /** 支持订单服务，用于查询和更新支持订单 */
    @Autowired
    private SupportOrderService supportOrderService;

    /** 资金流水服务，用于记录退款流水 */
    @Autowired
    private FundingLedgerService fundingLedgerService;

    /** 项目拨付服务，用于创建分阶段拨付记录 */
    @Autowired
    private ProjectPayoutService projectPayoutService;

    /** 系统用户服务，用于查询和更新用户余额 */
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private HeatCalculationService heatCalculationService;

    /**
     * 定时检查项目状态
     * <p>
     * 每分钟执行一次，查找所有状态为"筹款中"（status=1）且截止时间已到的项目，
     * 逐个处理其众筹结果。单个项目处理失败不影响其他项目的处理。
     * </p>
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkProjectStatus() {
        log.info("开始检查项目状态...");
        LocalDateTime now = LocalDateTime.now();

        // 查询状态为"筹款中"（1）且截止时间小于等于当前时间的项目
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getStatus, 1);
        wrapper.le(Project::getEndTime, now);

        List<Project> projects = projectService.list(wrapper);
        for (Project project : projects) {
            try {
                // 处理单个项目的众筹结果
                processProjectResult(project);
            } catch (Exception e) {
                // 单个项目处理失败不影响其他项目
                log.error("处理项目状态失败, projectId: {}", project.getId(), e);
            }
        }
        log.info("检查项目状态完成");
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void recalculateHeat() {
        log.info("开始定时重算项目热度...");
        try {
            int count = heatCalculationService.recalculateActiveProjectHeat();
            log.info("热度重算完成, 更新{}个项目", count);
        } catch (Exception e) {
            log.error("热度重算失败", e);
        }
    }

    /**
     * 处理单个项目的众筹结果
     * <p>
     * 根据当前筹款金额与目标金额的比较结果，判断项目是否众筹成功：
     * <ul>
     *   <li>当前金额 >= 目标金额：众筹成功，更新状态为5（已成功），生成分阶段拨付计划</li>
     *   <li>当前金额 < 目标金额：众筹失败，更新状态为6（筹款失败），自动退款所有已支付订单</li>
     * </ul>
     * 该方法使用事务保证数据一致性，任何异常将导致全部回滚。
     * </p>
     *
     * @param project 待处理的项目实体
     */
    @Transactional(rollbackFor = Exception.class)
    public void processProjectResult(Project project) {
        if (project.getCurrentAmount().compareTo(project.getTargetAmount()) >= 0) {
            // 众筹成功：当前筹款金额达到或超过目标金额
            log.info("项目众筹成功: {}", project.getId());
            project.setStatus(5); // 5-已成功
            projectService.updateById(project);

            // 生成阶段性拨付计划
            generatePayouts(project);
        } else {
            // 众筹失败：当前筹款金额未达到目标金额
            log.info("项目众筹失败: {}", project.getId());
            project.setStatus(6); // 6-筹款失败
            projectService.updateById(project);

            // 退款所有已支付的订单
            processRefunds(project);
        }
    }

    /**
     * 生成分阶段拨付计划
     * <p>
     * 众筹成功后，按照30%-40%-30%的比例将筹款金额分为三个阶段拨付给发起人：
     * <ul>
     *   <li>第一阶段（30%）：项目众筹成功后立即拨付</li>
     *   <li>第二阶段（40%）：项目中期汇报后拨付</li>
     *   <li>第三阶段（30%）：项目回报发放完毕后拨付（使用减法计算，确保金额精确无误差）</li>
     * </ul>
     * </p>
     *
     * @param project 众筹成功的项目实体
     */
    private void generatePayouts(Project project) {
        BigDecimal totalAmount = project.getCurrentAmount();

        // 第一阶段：项目完成后拨付30%
        ProjectPayout payout1 = new ProjectPayout();
        payout1.setProjectId(project.getId());
        payout1.setSponsorId(project.getSponsorId());
        payout1.setStage(1);
        payout1.setPayoutRatio(new BigDecimal("0.30"));
        payout1.setAmount(totalAmount.multiply(payout1.getPayoutRatio()));
        payout1.setStatus(0); // 0-待拨付
        payout1.setConditionDesc("项目众筹成功，首期拨付30%");
        projectPayoutService.save(payout1);

        // 第二阶段：中期拨付40%
        ProjectPayout payout2 = new ProjectPayout();
        payout2.setProjectId(project.getId());
        payout2.setSponsorId(project.getSponsorId());
        payout2.setStage(2);
        payout2.setPayoutRatio(new BigDecimal("0.40"));
        payout2.setAmount(totalAmount.multiply(payout2.getPayoutRatio()));
        payout2.setStatus(0); // 0-待拨付
        payout2.setConditionDesc("项目中期汇报，中期拨付40%");
        projectPayoutService.save(payout2);

        // 第三阶段：尾期拨付30%（使用总金额减去前两期，避免浮点精度误差）
        ProjectPayout payout3 = new ProjectPayout();
        payout3.setProjectId(project.getId());
        payout3.setSponsorId(project.getSponsorId());
        payout3.setStage(3);
        payout3.setPayoutRatio(new BigDecimal("0.30"));
        payout3.setAmount(totalAmount.subtract(payout1.getAmount()).subtract(payout2.getAmount())); // 用减法确保总额精确
        payout3.setStatus(0); // 0-待拨付
        payout3.setConditionDesc("项目回报发放完毕，尾期拨付30%");
        projectPayoutService.save(payout3);
    }

    /**
     * 处理众筹失败后的退款流程
     * <p>
     * 查找该项目下所有已支付（status=1）的支持订单，逐笔执行退款操作：
     * <ol>
     *   <li>将订单状态更新为"已退款"（status=3）</li>
     *   <li>在资金流水表中记录退款流水</li>
     *   <li>将退款金额返还到支持者的账户余额</li>
     * </ol>
     * </p>
     *
     * @param project 众筹失败的项目实体
     */
    private void processRefunds(Project project) {
        // 查询该项目下所有已支付的订单
        LambdaQueryWrapper<SupportOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(SupportOrder::getProjectId, project.getId());
        orderWrapper.eq(SupportOrder::getStatus, 1); // 1-已支付

        List<SupportOrder> orders = supportOrderService.list(orderWrapper);
        for (SupportOrder order : orders) {
            // 将订单状态更新为"已退款"
            order.setStatus(3); // 3-已退款
            supportOrderService.updateById(order);

            // 在资金流水表中记录退款信息
            FundingLedger ledger = new FundingLedger();
            ledger.setProjectId(project.getId());
            ledger.setOrderId(order.getId());
            ledger.setUserId(order.getUserId());
            ledger.setAmount(order.getAmount());
            ledger.setType(2); // 2-平台退款
            ledger.setStatus(1); // 1-成功
            // 获取用户昵称，用于流水备注
            SysUser user = sysUserService.getById(order.getUserId());
            String nickname = user != null ? user.getNickname() : "未知用户";
            ledger.setRemark(String.format("业务场景: 项目筹款失败自动退款, 资金流向: 平台 -> 用户[%s] -> 原项目[%s]", nickname, project.getTitle()));
            fundingLedgerService.save(ledger);

            // 将退款金额返还到用户余额
            if (user != null) {
                BigDecimal currentBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
                user.setBalance(currentBalance.add(order.getAmount()));
                sysUserService.updateById(user);
            }
        }
    }
}
