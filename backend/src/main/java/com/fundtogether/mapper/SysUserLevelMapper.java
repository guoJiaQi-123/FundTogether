package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.SysUserLevel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户等级Mapper接口
 * <p>
 * 提供对sys_user_level表的基础CRUD操作，
 * 继承MyBatis-Plus的BaseMapper，自动拥有增删改查能力。
 * </p>
 */
@Mapper
public interface SysUserLevelMapper extends BaseMapper<SysUserLevel> {
}
