package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.entity.SysUserLevel;
import com.fundtogether.mapper.SupportOrderMapper;
import com.fundtogether.mapper.SysUserLevelMapper;
import com.fundtogether.service.SysUserLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SysUserLevelServiceImpl extends ServiceImpl<SysUserLevelMapper, SysUserLevel> implements SysUserLevelService {

    private final SupportOrderMapper supportOrderMapper;

    @Override
    public SysUserLevel getCurrentUserLevel(Long userId) {
        // 1. Calculate the user's total support amount (status = 1)
        BigDecimal totalSupportAmount = supportOrderMapper.getUserTotalSupportAmount(userId);
        if (totalSupportAmount == null) {
            totalSupportAmount = BigDecimal.ZERO;
        }

        // 2. Query all levels, ordered by minAmount descending
        // The first one where totalSupportAmount >= minAmount is the user's current level
        LambdaQueryWrapper<SysUserLevel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(SysUserLevel::getMinAmount, totalSupportAmount)
                .orderByDesc(SysUserLevel::getMinAmount)
                .last("LIMIT 1");

        return this.getOne(queryWrapper);
    }
}
