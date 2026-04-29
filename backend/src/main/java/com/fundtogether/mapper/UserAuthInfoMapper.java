package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.UserAuthInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户实名认证信息Mapper接口
 * <p>
 * 提供对user_auth_info表的基础CRUD操作，
 * 继承MyBatis-Plus的BaseMapper，自动拥有增删改查能力。
 * </p>
 */
@Mapper
public interface UserAuthInfoMapper extends BaseMapper<UserAuthInfo> {
}
