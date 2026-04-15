package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("support_order")
public class SupportOrder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    
    private Long userId;
    
    private Long projectId;
    
    private BigDecimal amount;
    
    private String message;
    
    /**
     * 0-待支付 1-已支付 2-已取消 3-已退款
     */
    private Integer status;
    
    private String payChannel;
    
    private LocalDateTime payTime;
    
    /**
     * 发货状态：0-未发货，1-已发货
     */
    private Integer deliveryStatus;
    
    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;
    
    /**
     * 物流单号
     */
    private String expressNo;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(exist = false)
    private String projectName;
    
    @TableField(exist = false)
    private String userName;
}
