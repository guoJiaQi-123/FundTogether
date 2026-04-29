package com.fundtogether.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fundtogether.common.Result;
import com.fundtogether.entity.SysUserLevel;
import com.fundtogether.security.LoginUser;
import com.fundtogether.service.SysUserLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户等级控制器
 * <p>
 * 提供用户等级相关的API功能，包括：
 * <ul>
 *   <li>查询当前用户等级：获取当前登录用户的等级信息</li>
 *   <li>管理员分页查询等级列表：管理员分页查询所有用户等级配置</li>
 *   <li>管理员添加等级：新增用户等级配置</li>
 *   <li>管理员修改等级：更新用户等级配置</li>
 *   <li>管理员删除等级：删除指定用户等级配置</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api")
public class SysUserLevelController {

    /** 用户等级服务，处理用户等级的增删改查 */
    @Autowired
    private SysUserLevelService sysUserLevelService;

    /**
     * 获取当前登录用户ID
     *
     * @return 当前登录用户的ID
     */
    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getId();
    }

    /**
     * 查询当前登录用户的等级信息
     *
     * @return 当前用户的等级信息
     */
    @GetMapping("/user-level/current")
    public Result<SysUserLevel> getCurrentUserLevel() {
        try {
            Long userId = getCurrentUserId();
            SysUserLevel level = sysUserLevelService.getCurrentUserLevel(userId);
            return Result.success(level);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 管理员分页查询用户等级配置列表
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @return 分页用户等级配置列表
     */
    @GetMapping("/admin/sys-user-level/page")
    public Result<IPage<SysUserLevel>> getSysUserLevelPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<SysUserLevel> page = new Page<>(current, size);
        return Result.success(sysUserLevelService.page(page));
    }

    /**
     * 管理员添加用户等级配置
     *
     * @param sysUserLevel 用户等级实体，包含等级名称、所需经验值、权益描述等
     * @return 添加结果提示
     */
    @PostMapping("/admin/sys-user-level")
    public Result<?> addSysUserLevel(@RequestBody SysUserLevel sysUserLevel) {
        sysUserLevelService.save(sysUserLevel);
        return Result.success("添加成功");
    }

    /**
     * 管理员修改用户等级配置
     *
     * @param sysUserLevel 用户等级实体，包含ID及待修改字段
     * @return 修改结果提示
     */
    @PutMapping("/admin/sys-user-level")
    public Result<?> updateSysUserLevel(@RequestBody SysUserLevel sysUserLevel) {
        sysUserLevelService.updateById(sysUserLevel);
        return Result.success("修改成功");
    }

    /**
     * 管理员删除用户等级配置
     *
     * @param id 等级配置ID
     * @return 删除结果提示
     */
    @DeleteMapping("/admin/sys-user-level/{id}")
    public Result<?> deleteSysUserLevel(@PathVariable Long id) {
        sysUserLevelService.removeById(id);
        return Result.success("删除成功");
    }
}
