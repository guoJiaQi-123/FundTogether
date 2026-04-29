package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("heat_rule")
public class HeatRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String factorKey;

    private String factorName;

    private BigDecimal weight;

    private Integer enabled;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
