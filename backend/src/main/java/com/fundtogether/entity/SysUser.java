package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 系统用户实体
 * <p>
 * 平台核心用户实体，存储用户的账号信息、个人资料和钱包余额等。
 * 用户可扮演不同角色（普通用户、项目发起人、管理员），
 * 账号支持手机号和邮箱两种注册方式。
 * 对应数据库表：sys_user
 * </p>
 */
@Data
@TableName("sys_user")
public class SysUser {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号，可以是手机号或邮箱 */
    private String account;

    /** 登录密码（加密存储） */
    private String password;

    /** 用户昵称，展示在个人主页和评论区的名称 */
    private String nickname;

    /** 用户头像URL地址 */
    private String avatar;

    /** 手机号码 */
    private String phone;

    /** 邮箱地址 */
    private String email;

    /** 用户角色：0-普通用户，1-项目发起人，2-管理员 */
    private Integer role;

    /** 账号状态：0-正常，1-禁用 */
    private Integer status;

    /** 禁用原因，管理员禁用账号时填写的理由 */
    private String disableReason;

    /** 个人简介，用户对自己的简短介绍 */
    private String bio;

    /** 教育背景，如"北京大学·计算机科学" */
    private String education;

    /** 出生日期 */
    private LocalDate birthday;

    /** 所在公司/组织名称 */
    private String company;

    /** 职业名称，如"软件工程师" */
    private String profession;

    /** 所在地，如"北京市·海淀区" */
    private String location;

    /** 性别：0-未知，1-男，2-女 */
    private Integer gender;

    /** 钱包余额（单位：元），用于平台内充值和提现 */
    private BigDecimal balance;

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
