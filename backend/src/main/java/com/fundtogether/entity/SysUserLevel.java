package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户等级实体
 * <p>
 * 定义平台的用户等级体系，根据用户累计支持金额划分不同等级。
 * 每个等级有对应的名称、金额区间、图标和描述，
 * 用于激励用户参与众筹并给予等级标识。
 * 对应数据库表：sys_user_level
 * </p>
 */
@Data
@TableName("sys_user_level")
public class SysUserLevel {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 等级名称，如"青铜支持者"、"白银支持者"、"黄金支持者"等 */
    private String levelName;

    /** 该等级的最低累计支持金额（单位：元） */
    private BigDecimal minAmount;

    /** 该等级的最高累计支持金额（单位：元），null表示无上限 */
    private BigDecimal maxAmount;

    /** 等级图标URL地址 */
    private String icon;

    /** 等级描述说明 */
    private String description;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
