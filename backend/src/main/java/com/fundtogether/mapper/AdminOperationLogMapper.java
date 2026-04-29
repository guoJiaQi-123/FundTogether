package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.AdminOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员操作日志Mapper接口
 * <p>
 * 提供对admin_operation_log表的基础CRUD操作，
 * 继承MyBatis-Plus的BaseMapper，自动拥有增删改查能力。
 * </p>
 */
@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLog> {
}
