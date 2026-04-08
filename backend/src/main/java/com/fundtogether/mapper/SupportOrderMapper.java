package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.SupportOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface SupportOrderMapper extends BaseMapper<SupportOrder> {

    @Select("SELECT IFNULL(SUM(amount), 0) FROM support_order WHERE status = 1 AND deleted = 0")
    BigDecimal getTotalAmount();

    @Select("SELECT IFNULL(SUM(amount), 0) FROM support_order WHERE user_id = #{userId} AND status = 1 AND deleted = 0")
    BigDecimal getUserTotalSupportAmount(Long userId);
}

