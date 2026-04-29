package com.fundtogether.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建项目动态更新的数据传输对象
 * <p>
 * 用于项目发起人发布项目进展动态时提交的数据。
 * 项目动态是发起人向支持者汇报项目进展的方式，
 * 可包含文字内容和配图，帮助支持者了解项目最新情况。
 * </p>
 */
@Data
public class UpdateCreateDTO {
    /** 动态所属的项目ID，标识该动态关联哪个项目 */
    @NotNull(message = "项目ID不能为空") // 校验规则：项目ID为必填项，不允许为null
    private Long projectId;

    /** 动态标题，概括本次进展的核心内容 */
    @NotBlank(message = "标题不能为空") // 校验规则：动态标题为必填项，不允许为null、空字符串或纯空白
    private String title;

    /** 动态正文内容，描述项目进展的详细情况 */
    @NotBlank(message = "内容不能为空") // 校验规则：动态内容为必填项，不允许为null、空字符串或纯空白
    private String content;

    /** 动态配图，多张图片URL以逗号分隔，可选字段 */
    private String images;
}
