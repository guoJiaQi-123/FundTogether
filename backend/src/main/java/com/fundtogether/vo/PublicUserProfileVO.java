package com.fundtogether.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 公开用户资料视图对象
 * <p>
 * 用于返回用户的公开资料信息，供其他用户查看。
 * 刻意排除了敏感字段（如账号、手机号、邮箱、密码、余额、角色、状态等），
 * 仅展示可公开的个人资料信息。
 * </p>
 */
@Data
public class PublicUserProfileVO {
    /** 用户ID */
    private Long id;
    /** 用户昵称 */
    private String nickname;
    /** 用户头像URL */
    private String avatar;
    /** 用户个人简介 */
    private String bio;
    /** 性别（0-未知, 1-男, 2-女） */
    private Integer gender;
    /** 生日 */
    private LocalDate birthday;
    /** 学历 */
    private String education;
    /** 职业 */
    private String profession;
    /** 公司 */
    private String company;
    /** 所在地 */
    private String location;
    /** 注册时间 */
    private LocalDateTime createdAt;
    /** 该用户的关注数 */
    private Long followingCount;
    /** 该用户的粉丝数 */
    private Long followerCount;
}
