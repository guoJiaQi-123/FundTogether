package com.fundtogether.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fundtogether.entity.WithdrawalOrder;
import java.math.BigDecimal;

public interface WithdrawalOrderService extends IService<WithdrawalOrder> {
    void applyWithdrawal(Long userId, BigDecimal amount, Integer type, String accountName, String accountNo, String bankName);
    IPage<WithdrawalOrder> getMyWithdrawals(Long userId, Integer current, Integer size);
    IPage<WithdrawalOrder> getAllWithdrawals(Integer current, Integer size);
    void approveWithdrawal(Long id, Long adminId);
    void rejectWithdrawal(Long id, Long adminId, String reason);
}
