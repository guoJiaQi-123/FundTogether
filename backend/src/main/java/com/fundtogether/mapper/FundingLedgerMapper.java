package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.FundingLedger;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资金流水账本Mapper接口
 * <p>
 * 提供对funding_ledger表的基础CRUD操作，
 * 继承MyBatis-Plus的BaseMapper，自动拥有增删改查能力。
 * </p>
 */
@Mapper
public interface FundingLedgerMapper extends BaseMapper<FundingLedger> {
}
