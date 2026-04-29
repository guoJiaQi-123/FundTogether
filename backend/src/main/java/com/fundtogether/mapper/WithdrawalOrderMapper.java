package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.WithdrawalOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提现订单Mapper接口
 * <p>
 * 提供对withdrawal_order表的基础CRUD操作，
 * 继承MyBatis-Plus的BaseMapper，自动拥有增删改查能力。
 * </p>
 */
@Mapper
public interface WithdrawalOrderMapper extends BaseMapper<WithdrawalOrder> {
}
