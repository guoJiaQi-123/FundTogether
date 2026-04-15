package com.fundtogether.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SupporterVO {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private BigDecimal amount;
    private String message;
    private LocalDateTime payTime;
    
    // 新增发货字段
    private Integer deliveryStatus;
    private LocalDateTime deliveryTime;
    private String expressNo;
}