package com.fundtogether.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员修改项目内容的数据传输对象
 * <p>
 * 用于管理员后台编辑项目的核心内容信息，包括项目简介、详情、视频、起止时间等。
 * 与项目创建/更新DTO不同，此DTO侧重于内容层面的修改，而非项目基本属性。
 * </p>
 */
@Data
public class AdminProjectContentUpdateDTO {
    /** 项目唯一标识ID */
    @NotNull(message = "项目ID不能为空") // 校验规则：项目ID为必填项，不允许为null
    private Long id;

    /** 项目简介，用于列表页和搜索结果展示的简短描述 */
    @NotBlank(message = "项目简介不能为空") // 校验规则：项目简介为必填项，不允许为null、空字符串或纯空白
    private String summary;

    /** 项目宣传视频URL地址，可选字段 */
    private String videoUrl;

    /** 项目详情，富文本格式的项目完整描述内容 */
    @NotBlank(message = "项目详情不能为空") // 校验规则：项目详情为必填项，不允许为null、空字符串或纯空白
    private String content;

    /** 项目众筹开始时间，可选字段，不填则由系统自动设定 */
    private LocalDateTime startTime;

    /** 项目众筹截止时间，超过此时间后将无法继续筹资 */
    @NotNull(message = "截止时间不能为空") // 校验规则：截止时间为必填项，不允许为null
    private LocalDateTime endTime;
}
