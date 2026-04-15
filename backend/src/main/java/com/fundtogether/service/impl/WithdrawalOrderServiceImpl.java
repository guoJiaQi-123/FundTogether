package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.common.exception.BusinessException;
import com.fundtogether.entity.FundingLedger;
import com.fundtogether.entity.SysUser;
import com.fundtogether.entity.WithdrawalOrder;
import com.fundtogether.mapper.SysUserMapper;
import com.fundtogether.mapper.WithdrawalOrderMapper;
import com.fundtogether.service.FundingLedgerService;
import com.fundtogether.service.SysUserService;
import com.fundtogether.service.WithdrawalOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class WithdrawalOrderServiceImpl extends ServiceImpl<WithdrawalOrderMapper, WithdrawalOrder> implements WithdrawalOrderService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private FundingLedgerService fundingLedgerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyWithdrawal(Long userId, BigDecimal amount, Integer type, String accountName, String accountNo, String bankName) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("提现金额必须大于0");
        }

        SysUser user = sysUserService.getById(userId);
        BigDecimal balance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
        if (balance.compareTo(amount) < 0) {
            throw new BusinessException("余额不足");
        }

        int rows = sysUserMapper.deductBalance(userId, amount);
        if (rows == 0) {
            throw new BusinessException("余额不足");
        }

        WithdrawalOrder order = new WithdrawalOrder();
        order.setOrderNo("WD" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        order.setUserId(userId);
        order.setAmount(amount);
        order.setType(type);
        order.setAccountName(accountName);
        order.setAccountNo(accountNo);
        order.setBankName(bankName);
        order.setStatus(0);
        this.save(order);

        FundingLedger ledger = new FundingLedger();
        ledger.setUserId(userId);
        ledger.setAmount(amount);
        ledger.setType(4);
        ledger.setStatus(0);
        ledger.setRemark(String.format("业务场景: 发起人申请提现, 资金流向: 发起人[%s] -> 提现至%s账户[%s]",
                user.getNickname(), type == 1 ? "支付宝" : "银行卡", accountNo));
        fundingLedgerService.save(ledger);
    }

    @Override
    public IPage<WithdrawalOrder> getMyWithdrawals(Long userId, Integer current, Integer size) {
        Page<WithdrawalOrder> page = new Page<>(current, size);
        LambdaQueryWrapper<WithdrawalOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WithdrawalOrder::getUserId, userId);
        wrapper.orderByDesc(WithdrawalOrder::getCreatedAt);
        return this.page(page, wrapper);
    }

    @Override
    public IPage<WithdrawalOrder> getAllWithdrawals(Integer current, Integer size) {
        Page<WithdrawalOrder> page = new Page<>(current, size);
        LambdaQueryWrapper<WithdrawalOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(WithdrawalOrder::getCreatedAt);
        IPage<WithdrawalOrder> result = this.page(page, wrapper);
        for (WithdrawalOrder order : result.getRecords()) {
            SysUser user = sysUserService.getById(order.getUserId());
            if (user != null) {
                order.setUserName(user.getNickname() != null ? user.getNickname() : user.getAccount());
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveWithdrawal(Long id, Long adminId) {
        WithdrawalOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException("提现订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("该提现订单已处理");
        }

        order.setStatus(1);
        order.setProcessTime(LocalDateTime.now());
        this.updateById(order);

        FundingLedger ledger = new FundingLedger();
        ledger.setUserId(order.getUserId());
        ledger.setAmount(order.getAmount());
        ledger.setType(4);
        ledger.setStatus(1);
        SysUser user = sysUserService.getById(order.getUserId());
        String userName = user != null ? user.getNickname() : "未知用户";
        ledger.setRemark(String.format("业务场景: 管理员审批提现通过, 资金流向: 平台 -> 发起人[%s] -> %s账户[%s]",
                userName, order.getType() == 1 ? "支付宝" : "银行卡", order.getAccountNo()));
        fundingLedgerService.save(ledger);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectWithdrawal(Long id, Long adminId, String reason) {
        WithdrawalOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException("提现订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("该提现订单已处理");
        }

        order.setStatus(2);
        order.setRejectReason(reason);
        order.setProcessTime(LocalDateTime.now());
        this.updateById(order);

        sysUserMapper.addBalance(order.getUserId(), order.getAmount());
    }
}
