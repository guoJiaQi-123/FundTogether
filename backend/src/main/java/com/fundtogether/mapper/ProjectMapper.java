package com.fundtogether.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fundtogether.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

        @Select("SELECT c.name as categoryName, COUNT(p.id) as projectCount, IFNULL(SUM(p.current_amount), 0) as totalAmount "
                        +
                        "FROM project_category c " +
                        "LEFT JOIN project p ON c.id = p.category_id AND p.deleted = 0 " +
                        "WHERE c.deleted = 0 " +
                        "GROUP BY c.id, c.name")
        List<Map<String, Object>> getCategoryStats();

        @Update("UPDATE project SET current_amount = current_amount + #{amount}, supporter_count = supporter_count + 1 WHERE id = #{projectId} AND deleted = 0")
        int incrementFunding(@Param("projectId") Long projectId, @Param("amount") java.math.BigDecimal amount);
}
