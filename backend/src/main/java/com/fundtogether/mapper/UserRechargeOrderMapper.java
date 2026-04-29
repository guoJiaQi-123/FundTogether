package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.UserRechargeOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户充值订单Mapper接口
 * <p>
 * 提供对user_recharge_order表的基础CRUD操作，
 * 继承MyBatis-Plus的BaseMapper，自动拥有增删改查能力。
 * </p>
 */
@Mapper
public interface UserRechargeOrderMapper extends BaseMapper<UserRechargeOrder> {
}
