package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("project_payout")
public class ProjectPayout {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long sponsorId;
    private Integer stage; // 1-首期, 2-中期, 3-尾期等
    private BigDecimal amount;
    private BigDecimal payoutRatio;
    private Integer status; // 0-待拨付, 1-已拨付
    private String conditionDesc;
    private LocalDateTime payoutTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
