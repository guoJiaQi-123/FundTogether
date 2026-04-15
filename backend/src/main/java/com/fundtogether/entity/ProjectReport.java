package com.fundtogether.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("project_report")
public class ProjectReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reporterId;
    private Long projectId;
    private String reason;
    private Integer status;
    private String handleResult;
    private Long handlerId;
    private LocalDateTime handleTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String reporterName;
    @TableField(exist = false)
    private String projectName;
}
