package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.SysNotice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统公告Mapper接口
 * <p>
 * 提供对sys_notice表的基础CRUD操作，
 * 继承MyBatis-Plus的BaseMapper，自动拥有增删改查能力。
 * </p>
 */
@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {
}
