package com.fundtogether.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 更新项目的数据传输对象
 * <p>
 * 用于项目发起人修改已有项目信息时提交的数据。
 * 继承自ProjectCreateDTO，包含项目的所有可编辑字段，
 * 并额外增加项目ID字段用于定位要更新的项目。
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectUpdateDTO extends ProjectCreateDTO {
    /** 要更新的项目唯一标识ID */
    @NotNull(message = "项目ID不能为空") // 校验规则：项目ID为必填项，不允许为null
    private Long id;
}
