package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.entity.FundingLedger;
import com.fundtogether.mapper.FundingLedgerMapper;
import com.fundtogether.service.FundingLedgerService;
import org.springframework.stereotype.Service;

@Service
public class FundingLedgerServiceImpl extends ServiceImpl<FundingLedgerMapper, FundingLedger> implements FundingLedgerService {
}
