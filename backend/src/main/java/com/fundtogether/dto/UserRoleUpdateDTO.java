package com.fundtogether.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户角色更新的数据传输对象
 * <p>
 * 用于管理员修改用户角色时提交的数据。
 * 通过指定用户ID和目标角色，实现用户权限的变更，
 * 如将普通用户升级为项目发起人或管理员等。
 * </p>
 */
@Data
public class UserRoleUpdateDTO {
    /** 要修改角色的用户ID */
    @NotNull(message = "用户ID不能为空") // 校验规则：用户ID为必填项，不允许为null
    private Long userId;

    /** 目标角色值，对应系统中的角色枚举 */
    @NotNull(message = "角色不能为空") // 校验规则：角色为必填项，不允许为null
    private Integer role;
}
