package com.fundtogether.controller;

import com.fundtogether.common.Result;
import com.fundtogether.dto.UpdateCreateDTO;
import com.fundtogether.entity.ProjectUpdate;
import com.fundtogether.security.LoginUser;
import com.fundtogether.service.ProjectUpdateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目动态控制器
 * <p>
 * 提供项目动态（项目进展更新）相关的API功能，包括：
 * <ul>
 *   <li>发布动态：项目发起人发布项目进展动态</li>
 *   <li>查询动态列表：公开接口，查询指定项目的所有动态</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api")
public class ProjectUpdateController {

    /** 项目动态服务，处理动态的发布与查询 */
    @Autowired
    private ProjectUpdateService projectUpdateService;

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
     * 发布项目动态
     * <p>
     * 项目发起人发布项目进展动态，包含标题、内容和图片。
     * </p>
     *
     * @param dto           动态创建DTO，包含projectId、title、content、images
     * @param bindingResult 参数校验结果
     * @return 动态发布结果提示
     */
    @PostMapping("/sponsor/update/publish")
    public Result<?> publishUpdate(@RequestBody @Valid UpdateCreateDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        projectUpdateService.publishUpdate(getCurrentUserId(), dto.getProjectId(), dto.getTitle(), dto.getContent(), dto.getImages());
        return Result.success("动态发布成功");
    }

    /**
     * 查询指定项目的动态列表
     * <p>
     * 公开接口，无需登录，返回指定项目的所有进展动态。
     * </p>
     *
     * @param projectId 项目ID
     * @return 项目动态列表
     */
    @GetMapping("/public/project/{projectId}/updates")
    public Result<List<ProjectUpdate>> getProjectUpdates(@PathVariable Long projectId) {
        return Result.success(projectUpdateService.getProjectUpdates(projectId));
    }
}
