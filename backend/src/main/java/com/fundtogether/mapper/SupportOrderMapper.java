package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.SupportOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 支持订单Mapper接口
 * <p>
 * 提供对support_order表的基础CRUD操作及自定义统计查询。
 * 包含平台总筹款金额、用户累计支持金额、筹款趋势等统计方法，
 * 用于管理后台数据看板展示。
 * </p>
 */
@Mapper
public interface SupportOrderMapper extends BaseMapper<SupportOrder> {

        /**
         * 查询平台所有已支付订单的总筹款金额
         * <p>
         * 统计所有状态为1（已支付）的订单金额总和，
         * 用于管理后台展示平台累计筹款总额。
         * </p>
         *
         * @return 平台总筹款金额，无数据时返回0
         */
        @Select("SELECT IFNULL(SUM(amount), 0) FROM support_order WHERE status = 1 AND deleted = 0")
        BigDecimal getTotalAmount();

        /**
         * 查询指定用户的累计支持金额
         * <p>
         * 统计该用户所有状态为1（已支付）的订单金额总和，
         * 用于计算用户等级和展示个人支持记录。
         * </p>
         *
         * @param userId 用户ID
         * @return 该用户的累计支持金额，无数据时返回0
         */
        @Select("SELECT IFNULL(SUM(amount), 0) FROM support_order WHERE user_id = #{userId} AND status = 1 AND deleted = 0")
        BigDecimal getUserTotalSupportAmount(Long userId);

        /**
         * 查询近6个月的筹款趋势数据
         * <p>
         * 按月分组统计近6个月（含当月）已支付订单的筹款金额，
         * 用于管理后台数据看板展示筹款趋势折线图。
         * </p>
         *
         * @return 月度筹款趋势列表，每项包含month（月份）和totalAmount（当月总额）
         */
        @Select("SELECT DATE_FORMAT(pay_time, '%Y-%m') as month, SUM(amount) as totalAmount " +
                        "FROM support_order " +
                        "WHERE status = 1 AND deleted = 0 AND pay_time >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 MONTH), '%Y-%m-01') "
                        +
                        "GROUP BY month " +
                        "ORDER BY month ASC")
        java.util.List<java.util.Map<String, Object>> getFundingTrend();

        /**
         * 按日统计近N天的每日筹款金额与订单数
         */
        @Select("SELECT DATE_FORMAT(pay_time, '%Y-%m-%d') as day, " +
                        "IFNULL(SUM(amount), 0) as totalAmount, COUNT(*) as orderCount " +
                        "FROM support_order " +
                        "WHERE status = 1 AND deleted = 0 AND pay_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
                        "GROUP BY day ORDER BY day ASC")
        java.util.List<java.util.Map<String, Object>> getDailyFundingTrend(@Param("days") int days);

        /**
         * 按小时分布统计近N天订单支付时段热力
         */
        @Select("SELECT HOUR(pay_time) as hour, COUNT(*) as orderCount, IFNULL(SUM(amount),0) as totalAmount " +
                        "FROM support_order WHERE status = 1 AND deleted = 0 " +
                        "AND pay_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) GROUP BY HOUR(pay_time) ORDER BY hour ASC")
        java.util.List<java.util.Map<String, Object>> getHourlyDistribution(@Param("days") int days);

        /**
         * Top N 筹款项目
         */
        @Select("SELECT p.id, p.title, p.current_amount as currentAmount, p.target_amount as targetAmount, p.supporter_count as supporterCount, p.heat "
                        +
                        "FROM project p WHERE p.deleted = 0 AND p.status IN (1,5) " +
                        "ORDER BY p.current_amount DESC LIMIT #{limit}")
        java.util.List<java.util.Map<String, Object>> getTopProjects(@Param("limit") int limit);

        /**
         * Top N 支持用户（累计金额）
         */
        @Select("SELECT u.id, u.nickname, u.avatar, IFNULL(SUM(o.amount),0) as totalAmount, COUNT(o.id) as orderCount "
                        +
                        "FROM support_order o INNER JOIN sys_user u ON o.user_id = u.id " +
                        "WHERE o.status = 1 AND o.deleted = 0 AND u.deleted = 0 " +
                        "GROUP BY u.id, u.nickname, u.avatar ORDER BY totalAmount DESC LIMIT #{limit}")
        java.util.List<java.util.Map<String, Object>> getTopSupporters(@Param("limit") int limit);

        /**
         * 今日支付订单数
         */
        @Select("SELECT COUNT(*) FROM support_order WHERE status = 1 AND deleted = 0 AND DATE(pay_time) = CURDATE()")
        Long getTodayOrderCount();

        /**
         * 今日支付金额
         */
        @Select("SELECT IFNULL(SUM(amount),0) FROM support_order WHERE status = 1 AND deleted = 0 AND DATE(pay_time) = CURDATE()")
        BigDecimal getTodayAmount();

        /**
         * 昨日支付金额（用于环比）
         */
        @Select("SELECT IFNULL(SUM(amount),0) FROM support_order WHERE status = 1 AND deleted = 0 AND DATE(pay_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
        BigDecimal getYesterdayAmount();

        /**
         * 获取用户近6个月月度支持趋势
         */
        @Select("SELECT DATE_FORMAT(pay_time, '%Y-%m') as month, SUM(amount) as totalAmount, COUNT(*) as orderCount " +
                        "FROM support_order " +
                        "WHERE user_id = #{userId} AND status = 1 AND deleted = 0 " +
                        "AND pay_time >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 MONTH), '%Y-%m-01') " +
                        "GROUP BY month ORDER BY month ASC")
        java.util.List<java.util.Map<String, Object>> getUserMonthlyTrend(@Param("userId") Long userId);

        /**
         * 获取用户支持项目分布（Top 10）
         */
        @Select("SELECT p.id, p.title, SUM(o.amount) as totalAmount, COUNT(o.id) as orderCount " +
                        "FROM support_order o " +
                        "INNER JOIN project p ON o.project_id = p.id " +
                        "WHERE o.user_id = #{userId} AND o.status = 1 AND o.deleted = 0 AND p.deleted = 0 " +
                        "GROUP BY p.id, p.title ORDER BY totalAmount DESC LIMIT 10")
        java.util.List<java.util.Map<String, Object>> getUserProjectDistribution(@Param("userId") Long userId);
}
