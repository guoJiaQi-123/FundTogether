package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员操作日志实体
 * <p>
 * 记录管理员在后台系统中的所有关键操作，包括操作人、操作类型、操作对象等信息，
 * 用于系统审计和操作追溯，确保管理行为的可追溯性。
 * 对应数据库表：admin_operation_log
 * </p>
 */
@Data
@TableName("admin_operation_log")
public class AdminOperationLog {
    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作管理员ID，关联sys_user表 */
    private Long adminId;

    /** 操作管理员姓名，冗余存储便于查询展示 */
    private String adminName;

    /** 操作类型，如"审核项目"、"禁用用户"等 */
    private String operation;

    /** 操作对象，如项目ID、用户ID等 */
    private String target;

    /** 操作详情，记录操作的具体内容 */
    private String detail;

    /** 操作者IP地址，用于安全审计 */
    private String ip;

    /** 操作时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
