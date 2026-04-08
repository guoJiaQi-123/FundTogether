package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_user_level")
public class SysUserLevel {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String levelName;
    
    private BigDecimal minAmount;
    
    private BigDecimal maxAmount;
    
    private String icon;
    
    private String description;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
