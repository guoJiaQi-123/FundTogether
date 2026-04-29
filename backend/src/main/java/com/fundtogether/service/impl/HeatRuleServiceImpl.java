package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.entity.HeatRule;
import com.fundtogether.mapper.HeatRuleMapper;
import com.fundtogether.service.HeatRuleService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class HeatRuleServiceImpl extends ServiceImpl<HeatRuleMapper, HeatRule> implements HeatRuleService {

    @Override
    public List<HeatRule> listEnabledRules() {
        LambdaQueryWrapper<HeatRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HeatRule::getEnabled, 1);
        wrapper.orderByAsc(HeatRule::getId);
        return this.list(wrapper);
    }

    @Override
    public void updateRuleWeight(Long id, BigDecimal weight) {
        HeatRule rule = this.getById(id);
        if (rule == null) {
            throw new RuntimeException("规则不存在");
        }
        if (weight.compareTo(BigDecimal.ZERO) < 0 || weight.compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("权重必须在0-100之间");
        }
        rule.setWeight(weight);
        this.updateById(rule);
    }

    @Override
    public void toggleRule(Long id, boolean enabled) {
        HeatRule rule = this.getById(id);
        if (rule == null) {
            throw new RuntimeException("规则不存在");
        }
        rule.setEnabled(enabled ? 1 : 0);
        this.updateById(rule);
    }

    @Override
    public void resetToDefault() {
        resetSingle("supporter_count", new BigDecimal("2.00"), true);
        resetSingle("funding_amount", new BigDecimal("0.10"), true);
        resetSingle("funding_progress", new BigDecimal("1.00"), true);
        resetSingle("favorite_count", new BigDecimal("1.50"), true);
        resetSingle("comment_count", new BigDecimal("0.50"), true);
        resetSingle("time_decay", new BigDecimal("1.00"), true);
    }

    private void resetSingle(String factorKey, BigDecimal weight, boolean enabled) {
        LambdaQueryWrapper<HeatRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HeatRule::getFactorKey, factorKey);
        HeatRule rule = this.getOne(wrapper);
        if (rule != null) {
            rule.setWeight(weight);
            rule.setEnabled(enabled ? 1 : 0);
            this.updateById(rule);
        }
    }
}
