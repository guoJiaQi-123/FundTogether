package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.common.enums.LedgerType;
import com.fundtogether.common.enums.OrderStatus;
import com.fundtogether.common.exception.BusinessException;
import com.fundtogether.dto.SupportOrderCreateDTO;
import com.fundtogether.entity.Project;
import com.fundtogether.entity.SupportOrder;
import com.fundtogether.mapper.ProjectMapper;
import com.fundtogether.mapper.SupportOrderMapper;
import com.fundtogether.mapper.SysUserMapper;
import com.fundtogether.entity.SysUser;
import com.fundtogether.service.ProjectService;
import com.fundtogether.service.SupportOrderService;
import com.fundtogether.service.SysUserService;
import com.fundtogether.service.FundingLedgerService;
import com.fundtogether.utils.RedisLockUtil;
import com.fundtogether.vo.SupporterVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

import com.fundtogether.entity.FundingLedger;

@Service
public class SupportOrderServiceImpl extends ServiceImpl<SupportOrderMapper, SupportOrder>
        implements SupportOrderService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private FundingLedgerService fundingLedgerService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private RedisLockUtil redisLockUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(SupportOrderCreateDTO dto, Long userId) {
        String lockKey = "order:lock:" + userId + ":" + dto.getProjectId();
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisLockUtil.tryLock(lockKey, lockValue, 10);
        if (!locked) {
            throw new BusinessException("操作过于频繁，请稍后再试");
        }

        try {
            doCreateOrder(dto, userId);
        } finally {
            redisLockUtil.unlock(lockKey, lockValue);
        }
    }

    private void doCreateOrder(SupportOrderCreateDTO dto, Long userId) {
        Project project = projectService.getProjectDetail(dto.getProjectId());
        if (project.getStatus() != 1) {
            throw new BusinessException("该项目当前不支持投资");
        }

        int rows = sysUserMapper.deductBalance(userId, dto.getAmount());
        if (rows == 0) {
            throw new BusinessException("余额不足，请先充值");
        }

        SupportOrder order = new SupportOrder();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setUserId(userId);
        order.setProjectId(dto.getProjectId());
        order.setAmount(dto.getAmount());
        order.setMessage(dto.getMessage());
        order.setPayChannel("3");
        order.setStatus(OrderStatus.PAID.getCode());
        order.setPayTime(java.time.LocalDateTime.now());
        this.save(order);

        FundingLedger ledger = new FundingLedger();
        ledger.setProjectId(project.getId());
        ledger.setOrderId(order.getId());
        ledger.setUserId(userId);
        ledger.setAmount(order.getAmount());
        ledger.setType(LedgerType.USER_PAYMENT.getCode());
        ledger.setStatus(1);
        SysUser user = sysUserService.getById(userId);
        String nickname = user != null ? user.getNickname() : "用户";
        ledger.setRemark(String.format("业务场景: 支持项目[%s], 资金流向: 用户[%s] -> 平台 -> 项目[%s]", project.getTitle(), nickname,
                project.getTitle()));
        fundingLedgerService.save(ledger);

        projectMapper.incrementFunding(dto.getProjectId(), dto.getAmount());
    }

    @Override
    public IPage<SupportOrder> getMyOrders(Long userId, Integer current, Integer size) {
        Page<SupportOrder> page = new Page<>(current, size);
        LambdaQueryWrapper<SupportOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupportOrder::getUserId, userId);
        wrapper.orderByDesc(SupportOrder::getCreatedAt);

        IPage<SupportOrder> result = this.page(page, wrapper);
        for (SupportOrder order : result.getRecords()) {
            Project project = projectService.getById(order.getProjectId());
            if (project != null) {
                order.setProjectName(project.getTitle());
            }
        }
        return result;
    }

    @Override
    public IPage<SupporterVO> getProjectSupporters(Long projectId, Long sponsorId, Integer current, Integer size) {
        Project project = projectService.getById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        if (!project.getSponsorId().equals(sponsorId)) {
            throw new BusinessException("无权查看他人项目的支持者");
        }

        Page<SupportOrder> page = new Page<>(current, size);
        LambdaQueryWrapper<SupportOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupportOrder::getProjectId, projectId);
        wrapper.eq(SupportOrder::getStatus, OrderStatus.PAID.getCode());
        wrapper.orderByDesc(SupportOrder::getCreatedAt);
        IPage<SupportOrder> orderPage = this.page(page, wrapper);

        Page<SupporterVO> voPage = new Page<>(current, size, orderPage.getTotal());
        List<SupporterVO> voList = orderPage.getRecords().stream().map(order -> {
            SupporterVO vo = new SupporterVO();
            BeanUtils.copyProperties(order, vo);
            SysUser user = sysUserService.getById(order.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Map<String, Object> getMyStats(Long userId) {
        LambdaQueryWrapper<SupportOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupportOrder::getUserId, userId);
        wrapper.eq(SupportOrder::getStatus, OrderStatus.PAID.getCode());

        List<SupportOrder> orders = this.list(wrapper);

        BigDecimal totalAmount = BigDecimal.ZERO;
        long projectCount = orders.stream().map(SupportOrder::getProjectId).distinct().count();

        for (SupportOrder order : orders) {
            totalAmount = totalAmount.add(order.getAmount());
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAmount", totalAmount);
        stats.put("projectCount", projectCount);

        return stats;
    }

    @Override
    public Project getProjectByOrderId(Long orderId) {
        SupportOrder order = this.getById(orderId);
        if (order != null) {
            return projectService.getById(order.getProjectId());
        }
        return null;
    }
}
