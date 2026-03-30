package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("funding_ledger")
public class FundingLedger {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private Integer type; // 1-用户支付, 2-平台退款, 3-阶段拨付给发起人
    private Integer status; // 0-处理中, 1-成功, 2-失败
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
