package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实名认证信息实体
 * <p>
 * 存储用户提交的实名认证信息，包括真实姓名、身份证号及身份证照片等。
 * 用户提交认证后需经管理员审核，审核通过后用户获得实名认证标识，
 * 实名认证是发起项目和提现的前提条件。
 * 对应数据库表：user_auth_info
 * </p>
 */
@Data
@TableName("user_auth_info")
public class UserAuthInfo {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联用户ID，关联sys_user表 */
    private Long userId;

    /** 真实姓名，须与身份证上的姓名一致 */
    private String realName;

    /** 身份证号码，18位有效身份证号 */
    private String idCard;

    /** 身份证正面（人像面）照片URL地址 */
    private String idCardFront;

    /** 身份证反面（国徽面）照片URL地址 */
    private String idCardBack;

    /**
     * 审核状态：
     * 0-待审核（用户已提交，等待管理员审核）
     * 1-审核通过（认证成功）
     * 2-审核驳回（认证未通过，需重新提交）
     */
    private Integer status;

    /** 审核原因，审核通过/驳回时管理员填写的说明 */
    private String auditReason;

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
