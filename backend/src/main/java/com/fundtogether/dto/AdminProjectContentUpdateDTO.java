package com.fundtogether.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminProjectContentUpdateDTO {
    @NotNull(message = "项目ID不能为空")
    private Long id;

    @NotBlank(message = "项目简介不能为空")
    private String summary;

    private String videoUrl;

    @NotBlank(message = "项目详情不能为空")
    private String content;

    private LocalDateTime startTime;

    @NotNull(message = "截止时间不能为空")
    private LocalDateTime endTime;
}

