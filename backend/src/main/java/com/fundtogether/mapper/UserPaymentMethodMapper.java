package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.UserPaymentMethod;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户支付方式Mapper接口
 * <p>
 * 提供对user_payment_method表的基础CRUD操作，
 * 继承MyBatis-Plus的BaseMapper，自动拥有增删改查能力。
 * </p>
 */
@Mapper
public interface UserPaymentMethodMapper extends BaseMapper<UserPaymentMethod> {
}
