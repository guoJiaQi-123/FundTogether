package com.fundtogether.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户创建评论的数据传输对象
 * <p>
 * 用于用户对项目发表评论或回复他人评论时提交的数据。
 * 支持一级评论（直接对项目评论）和二级评论（回复某条评论），
 * 通过parentId字段区分是否为回复评论。
 * </p>
 */
@Data
public class CommentCreateDTO {
    /** 评论所属的项目ID，标识该评论关联哪个项目 */
    @NotNull(message = "项目ID不能为空") // 校验规则：项目ID为必填项，不允许为null
    private Long projectId;

    /** 父评论ID，回复他人评论时填写；为null时表示这是一条一级评论（直接对项目评论） */
    private Long parentId;

    /** 评论正文内容 */
    @NotBlank(message = "评论内容不能为空") // 校验规则：评论内容为必填项，不允许为null、空字符串或纯空白
    private String content;
}
