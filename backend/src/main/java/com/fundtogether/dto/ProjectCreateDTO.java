package com.fundtogether.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建项目的数据传输对象
 * <p>
 * 用于项目发起人提交新众筹项目时使用的数据结构。
 * 包含项目的基本信息、内容描述、目标金额、截止时间等核心字段。
 * 项目创建后默认进入待审核状态，需管理员审核通过后方可上线。
 * </p>
 */
@Data
public class ProjectCreateDTO {

    /** 项目标题，展示在项目列表和详情页的名称 */
    @NotBlank(message = "项目标题不能为空") // 校验规则：项目标题为必填项，不允许为null、空字符串或纯空白
    private String title;

    /** 项目简介，用于列表页和搜索结果展示的简短描述 */
    @NotBlank(message = "项目简介不能为空") // 校验规则：项目简介为必填项，不允许为null、空字符串或纯空白
    private String summary;

    /** 项目封面图片URL地址，用于项目列表和详情页的封面展示 */
    @NotBlank(message = "项目封面不能为空") // 校验规则：项目封面为必填项，不允许为null、空字符串或纯空白
    private String coverImage;

    /** 项目宣传视频URL地址，可选字段 */
    private String videoUrl;

    /** 项目详情，富文本格式的项目完整描述内容 */
    @NotBlank(message = "项目详情不能为空") // 校验规则：项目详情为必填项，不允许为null、空字符串或纯空白
    private String content;

    /** 项目众筹目标金额（单位：元），项目需在截止时间前达到此金额才算筹资成功 */
    @NotNull(message = "目标金额不能为空") // 校验规则：目标金额为必填项，不允许为null
    private BigDecimal targetAmount;

    /** 项目众筹截止时间，超过此时间后将无法继续筹资 */
    @NotNull(message = "截止时间不能为空") // 校验规则：截止时间为必填项，不允许为null
    private LocalDateTime endTime;

    /** 项目所属分类ID，关联项目分类表，可选字段 */
    private Long categoryId;
}
