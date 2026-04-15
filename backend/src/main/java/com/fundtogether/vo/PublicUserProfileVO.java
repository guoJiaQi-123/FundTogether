package com.fundtogether.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Public-facing user profile fields. Intentionally excludes sensitive fields
 * such as account/phone/email/password/balance/role/status.
 */
@Data
public class PublicUserProfileVO {
    private Long id;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer gender;
    private LocalDate birthday;
    private String education;
    private String profession;
    private String company;
    private String location;
    private LocalDateTime createdAt;
}

