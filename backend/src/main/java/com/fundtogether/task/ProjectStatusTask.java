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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class ProjectStatusTask {

    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private SupportOrderService supportOrderService;
    
    @Autowired
    private FundingLedgerService fundingLedgerService;
    
    @Autowired
    private ProjectPayoutService projectPayoutService;

    // Run every minute
    @Scheduled(cron = "0 * * * * ?")
    public void checkProjectStatus() {
        log.info("开始检查项目状态...");
        LocalDateTime now = LocalDateTime.now();
        
        // Find projects that are funding (status = 1) and end_time <= now
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getStatus, 1);
        wrapper.le(Project::getEndTime, now);
        
        List<Project> projects = projectService.list(wrapper);
        for (Project project : projects) {
            try {
                processProjectResult(project);
            } catch (Exception e) {
                log.error("处理项目状态失败, projectId: {}", project.getId(), e);
            }
        }
        log.info("检查项目状态完成");
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void processProjectResult(Project project) {
        if (project.getCurrentAmount().compareTo(project.getTargetAmount()) >= 0) {
            // Success
            log.info("项目众筹成功: {}", project.getId());
            project.setStatus(5); // 5-已成功
            projectService.updateById(project);
            
            // Generate phased payouts
            generatePayouts(project);
        } else {
            // Failed
            log.info("项目众筹失败: {}", project.getId());
            project.setStatus(6); // 6-筹款失败
            projectService.updateById(project);
            
            // Refund all orders
            processRefunds(project);
        }
    }
    
    private void generatePayouts(Project project) {
        BigDecimal totalAmount = project.getCurrentAmount();
        
        // Stage 1: 30% on completion
        ProjectPayout payout1 = new ProjectPayout();
        payout1.setProjectId(project.getId());
        payout1.setSponsorId(project.getSponsorId());
        payout1.setStage(1);
        payout1.setPayoutRatio(new BigDecimal("0.30"));
        payout1.setAmount(totalAmount.multiply(payout1.getPayoutRatio()));
        payout1.setStatus(0);
        payout1.setConditionDesc("项目众筹成功，首期拨付30%");
        projectPayoutService.save(payout1);
        
        // Stage 2: 40% mid-term
        ProjectPayout payout2 = new ProjectPayout();
        payout2.setProjectId(project.getId());
        payout2.setSponsorId(project.getSponsorId());
        payout2.setStage(2);
        payout2.setPayoutRatio(new BigDecimal("0.40"));
        payout2.setAmount(totalAmount.multiply(payout2.getPayoutRatio()));
        payout2.setStatus(0);
        payout2.setConditionDesc("项目中期汇报，中期拨付40%");
        projectPayoutService.save(payout2);
        
        // Stage 3: 30% final
        ProjectPayout payout3 = new ProjectPayout();
        payout3.setProjectId(project.getId());
        payout3.setSponsorId(project.getSponsorId());
        payout3.setStage(3);
        payout3.setPayoutRatio(new BigDecimal("0.30"));
        payout3.setAmount(totalAmount.subtract(payout1.getAmount()).subtract(payout2.getAmount())); // Ensure exact total
        payout3.setStatus(0);
        payout3.setConditionDesc("项目回报发放完毕，尾期拨付30%");
        projectPayoutService.save(payout3);
    }
    
    private void processRefunds(Project project) {
        LambdaQueryWrapper<SupportOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(SupportOrder::getProjectId, project.getId());
        orderWrapper.eq(SupportOrder::getStatus, 1); // 1-已支付
        
        List<SupportOrder> orders = supportOrderService.list(orderWrapper);
        for (SupportOrder order : orders) {
            // Update order status to refunded
            order.setStatus(3); // 3-已退款
            supportOrderService.updateById(order);
            
            // Record refund in funding_ledger
            FundingLedger ledger = new FundingLedger();
            ledger.setProjectId(project.getId());
            ledger.setOrderId(order.getId());
            ledger.setUserId(order.getUserId());
            ledger.setAmount(order.getAmount());
            ledger.setType(2); // 2-平台退款
            ledger.setStatus(1); // 1-成功
            ledger.setRemark("项目筹款失败自动退款，原订单号：" + order.getOrderNo());
            fundingLedgerService.save(ledger);
        }
    }
}
