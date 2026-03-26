package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("project_reward")
public class ProjectReward {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private BigDecimal amount;
    
    private String content;
    
    @TableField("reward_count")
    private Integer limitCount;
    
    @TableField(exist = false)
    private Integer currentCount;
    
    @TableField("delivery_time")
    private Integer returnTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
