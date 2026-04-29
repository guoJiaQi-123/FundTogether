package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 系统用户Mapper接口
 * <p>
 * 提供对sys_user表的基础CRUD操作及自定义更新方法。
 * 包含用户钱包余额的扣减和增加操作，
 * 使用SQL原子操作保证并发场景下的余额安全。
 * </p>
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 扣减用户钱包余额
     * <p>
     * 从指定用户的钱包余额中扣减指定金额，使用条件判断确保余额充足，
     * 防止余额出现负数。采用SQL原子操作保证并发安全。
     * </p>
     *
     * @param userId 用户ID
     * @param amount 扣减金额
     * @return 影响的行数，0表示扣减失败（余额不足或用户不存在）
     */
    @Update("UPDATE sys_user SET balance = balance - #{amount} WHERE id = #{userId} AND balance >= #{amount} AND deleted = 0")
    int deductBalance(@Param("userId") Long userId, @Param("amount") java.math.BigDecimal amount);

    /**
     * 增加用户钱包余额
     * <p>
     * 向指定用户的钱包余额中增加指定金额，
     * 用于充值成功后增加余额等场景。
     * </p>
     *
     * @param userId 用户ID
     * @param amount 增加金额
     * @return 影响的行数，0表示增加失败（用户不存在）
     */
    @Update("UPDATE sys_user SET balance = balance + #{amount} WHERE id = #{userId} AND deleted = 0")
    int addBalance(@Param("userId") Long userId, @Param("amount") java.math.BigDecimal amount);

    /**
     * 近N天新注册用户数趋势
     */
    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m-%d') as day, COUNT(*) as count " +
            "FROM sys_user WHERE deleted = 0 AND created_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY day ORDER BY day ASC")
    java.util.List<java.util.Map<String, Object>> getDailyUserRegistration(@Param("days") int days);

    /**
     * 用户角色分布（按按位角色掩码统计）
     */
    @Select("SELECT " +
            "SUM(CASE WHEN role & 1 > 0 THEN 1 ELSE 0 END) as userCount, " +
            "SUM(CASE WHEN role & 2 > 0 THEN 1 ELSE 0 END) as sponsorCount, " +
            "SUM(CASE WHEN role & 4 > 0 THEN 1 ELSE 0 END) as adminCount " +
            "FROM sys_user WHERE deleted = 0")
    java.util.Map<String, Object> getRoleDistribution();
}
