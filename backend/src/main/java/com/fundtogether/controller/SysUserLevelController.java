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

@RestController
@RequestMapping("/api")
public class SysUserLevelController {

    @Autowired
    private SysUserLevelService sysUserLevelService;

    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getId();
    }

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

    @GetMapping("/admin/sys-user-level/page")
    public Result<IPage<SysUserLevel>> getSysUserLevelPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<SysUserLevel> page = new Page<>(current, size);
        return Result.success(sysUserLevelService.page(page));
    }

    @PostMapping("/admin/sys-user-level")
    public Result<?> addSysUserLevel(@RequestBody SysUserLevel sysUserLevel) {
        sysUserLevelService.save(sysUserLevel);
        return Result.success("添加成功");
    }

    @PutMapping("/admin/sys-user-level")
    public Result<?> updateSysUserLevel(@RequestBody SysUserLevel sysUserLevel) {
        sysUserLevelService.updateById(sysUserLevel);
        return Result.success("修改成功");
    }

    @DeleteMapping("/admin/sys-user-level/{id}")
    public Result<?> deleteSysUserLevel(@PathVariable Long id) {
        sysUserLevelService.removeById(id);
        return Result.success("删除成功");
    }
}
