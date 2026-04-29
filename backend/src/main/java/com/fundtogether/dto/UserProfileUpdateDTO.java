package com.fundtogether.dto;

import lombok.Data;

/**
 * 用户个人资料更新的数据传输对象
 * <p>
 * 用于用户修改个人资料时提交的数据。
 * 所有字段均为可选，仅更新提交的非null字段，未提交的字段保持原值不变。
 * 支持修改昵称、头像、个人简介、教育背景、生日、公司、职业、所在地和性别等信息。
 * </p>
 */
@Data
public class UserProfileUpdateDTO {
    /** 用户昵称，展示在个人主页和评论区的名称 */
    private String nickname;
    /** 用户头像URL地址 */
    private String avatar;
    /** 个人简介，用户对自己的简短介绍 */
    private String bio;
    /** 教育背景，如"北京大学·计算机科学" */
    private String education;
    /** 出生日期，以字符串形式传输，后端解析为LocalDate */
    private String birthday;
    /** 所在公司/组织名称 */
    private String company;
    /** 职业名称，如"软件工程师" */
    private String profession;
    /** 所在地，如"北京市·海淀区" */
    private String location;
    /** 性别：0-未知，1-男，2-女 */
    private Integer gender;
}
