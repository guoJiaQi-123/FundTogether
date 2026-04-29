package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 众筹项目Mapper接口
 * <p>
 * 提供对project表的基础CRUD操作及自定义查询。
 * 包含按分类统计项目数据、递增项目筹款金额和支持者数量等业务方法。
 * </p>
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    /**
     * 按项目分类统计项目数量和累计筹资金额
     * <p>
     * 左连接project_category和project表，按分类分组统计，
     * 返回每个分类的名称、项目数量和累计筹款总额。
     * </p>
     *
     * @return 分类统计列表，每项包含categoryName、projectCount、totalAmount
     */
    @Select("SELECT c.name as categoryName, COUNT(p.id) as projectCount, IFNULL(SUM(p.current_amount), 0) as totalAmount "
                        +
                        "FROM project_category c " +
                        "LEFT JOIN project p ON c.id = p.category_id AND p.deleted = 0 " +
                        "WHERE c.deleted = 0 " +
                        "GROUP BY c.id, c.name")
    List<Map<String, Object>> getCategoryStats();

    /**
     * 递增项目的已筹金额和支持者数量
     * <p>
     * 当用户成功支持项目后调用，将项目的current_amount增加指定金额，
     * 同时supporter_count加1。使用SQL原子操作保证并发安全。
     * </p>
     *
     * @param projectId 项目ID
     * @param amount    递增的筹款金额
     * @return 影响的行数，0表示更新失败（项目不存在或已删除）
     */
    @Update("UPDATE project SET current_amount = current_amount + #{amount}, supporter_count = supporter_count + 1 WHERE id = #{projectId} AND deleted = 0")
    int incrementFunding(@Param("projectId") Long projectId, @Param("amount") java.math.BigDecimal amount);

    /**
     * 项目状态分布统计
     */
    @Select("SELECT status, COUNT(*) as count FROM project WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> getStatusDistribution();

    /**
     * 近N天新增项目趋势
     */
    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m-%d') as day, COUNT(*) as count " +
            "FROM project WHERE deleted = 0 AND created_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY day ORDER BY day ASC")
    List<Map<String, Object>> getDailyProjectCreation(@Param("days") int days);

    /**
     * 项目达标率（已达到目标金额的占比）
     */
    @Select("SELECT " +
            "SUM(CASE WHEN current_amount >= target_amount THEN 1 ELSE 0 END) as reached, " +
            "COUNT(*) as total FROM project WHERE deleted = 0 AND status IN (1,5,6)")
    Map<String, Object> getReachedRate();
}
