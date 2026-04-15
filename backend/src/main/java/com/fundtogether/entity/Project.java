package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long sponsorId;
    
    private Long categoryId;
    
    private String title;
    
    private String summary;
    
    private String coverImage;
    
    private String videoUrl;
    
    private String content;
    
    private BigDecimal targetAmount;
    
    private BigDecimal currentAmount;
    
    private Integer supporterCount;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    /**
     * 状态: 0-待审核, 1-筹款中, 2-已驳回, 3-已取消, 4-已下架, 5-已完成
     */
    private Integer status;
    
    @TableField(exist = false)
    private String auditReason;
    
    private Integer heat; // 热度
    
    @TableField(exist = false)
    private String sponsorName;

    @TableField(exist = false)
    private String sponsorAvatar;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
