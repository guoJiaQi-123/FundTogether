package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.SysUserMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户消息Mapper接口
 * <p>
 * 提供对sys_user_message表的基础CRUD操作，
 * 继承MyBatis-Plus的BaseMapper，自动拥有增删改查能力。
 * </p>
 */
@Mapper
public interface SysUserMessageMapper extends BaseMapper<SysUserMessage> {
}
