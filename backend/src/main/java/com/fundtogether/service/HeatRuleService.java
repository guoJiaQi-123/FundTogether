package com.fundtogether.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fundtogether.entity.HeatRule;

import java.util.List;

public interface HeatRuleService extends IService<HeatRule> {

    List<HeatRule> listEnabledRules();

    void updateRuleWeight(Long id, java.math.BigDecimal weight);

    void toggleRule(Long id, boolean enabled);

    void resetToDefault();
}
