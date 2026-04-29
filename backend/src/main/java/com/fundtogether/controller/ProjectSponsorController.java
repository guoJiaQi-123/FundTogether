package com.fundtogether.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fundtogether.common.Result;
import com.fundtogether.dto.ProjectCreateDTO;
import com.fundtogether.dto.ProjectUpdateDTO;
import com.fundtogether.entity.Project;
import com.fundtogether.security.LoginUser;
import com.fundtogether.service.ProjectService;
import com.fundtogether.service.SupportOrderService;
import com.fundtogether.vo.SupporterVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * 项目发起人控制器
 * <p>
 * 提供项目发起人（众筹项目创建者）相关的API功能，包括：
 * <ul>
 *   <li>创建项目：发起人提交新项目，进入审核状态</li>
 *   <li>修改待审核项目：修改尚未通过审核的项目信息</li>
 *   <li>修改已上线项目：修改已上线项目的内容</li>
 *   <li>取消项目：取消待审核的项目</li>
 *   <li>查询我的项目：分页查询当前发起人创建的所有项目</li>
 *   <li>查询项目支持者：分页查询指定项目的支持者列表</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/sponsor/project")
public class ProjectSponsorController {

    /** 项目服务，处理项目的创建、修改、取消等操作 */
    @Autowired
    private ProjectService projectService;

    /** 支持订单服务，用于查询项目支持者列表 */
    @Autowired
    private SupportOrderService supportOrderService;

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
     * 创建众筹项目
     * <p>
     * 发起人提交新项目，项目创建后进入待审核状态。
     * </p>
     *
     * @param dto           项目创建DTO，包含标题、摘要、详情、目标金额、起止时间等
     * @param bindingResult 参数校验结果
     * @return 项目提交结果提示
     */
    @PostMapping("/create")
    public Result<?> createProject(@RequestBody @Valid ProjectCreateDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        projectService.createProject(dto, getCurrentUserId());
        return Result.success("项目提交审核成功");
    }

    /**
     * 修改待审核项目
     * <p>
     * 仅允许修改尚未通过审核的项目信息。
     * </p>
     *
     * @param dto           项目更新DTO，包含项目ID及待修改字段
     * @param bindingResult 参数校验结果
     * @return 修改结果提示
     */
    @PutMapping("/update")
    public Result<?> updateProject(@RequestBody @Valid ProjectUpdateDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        projectService.updatePendingProject(dto, getCurrentUserId());
        return Result.success("项目修改成功");
    }

    /**
     * 修改已上线项目
     * <p>
     * 允许发起人修改已上线项目的内容，修改后可能需要重新审核。
     * </p>
     *
     * @param dto           项目更新DTO，包含项目ID及待修改字段
     * @param bindingResult 参数校验结果
     * @return 修改结果提示
     */
    @PutMapping("/update-active")
    public Result<?> updateActiveProject(@RequestBody @Valid ProjectUpdateDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        projectService.updateActiveProject(dto, getCurrentUserId());
        return Result.success("项目内容修改成功");
    }

    /**
     * 取消待审核项目
     * <p>
     * 发起人可取消自己创建的待审核项目。
     * </p>
     *
     * @param id 项目ID
     * @return 取消结果提示
     */
    @PutMapping("/cancel/{id}")
    public Result<?> cancelProject(@PathVariable Long id) {
        projectService.cancelPendingProject(id, getCurrentUserId());
        return Result.success("项目取消成功");
    }

    /**
     * 分页查询当前发起人的项目列表
     * <p>
     * 查询当前登录用户作为发起人创建的所有项目，按创建时间倒序排列。
     * </p>
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @return 分页项目列表
     */
    @GetMapping("/my-list")
    public Result<IPage<Project>> getMyProjects(@RequestParam(defaultValue = "1") Integer current,
                                                @RequestParam(defaultValue = "10") Integer size) {
        Page<Project> page = new Page<>(current, size);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getSponsorId, getCurrentUserId());
        wrapper.orderByDesc(Project::getCreatedAt);

        IPage<Project> result = projectService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 分页查询指定项目的支持者列表
     * <p>
     * 仅项目发起人可查看自己项目的支持者列表。
     * </p>
     *
     * @param projectId 项目ID
     * @param current   当前页码，默认1
     * @param size      每页条数，默认10
     * @return 分页支持者VO列表
     */
    @GetMapping("/{projectId}/supporters")
    public Result<IPage<SupporterVO>> getProjectSupporters(@PathVariable Long projectId,
                                                           @RequestParam(defaultValue = "1") Integer current,
                                                           @RequestParam(defaultValue = "10") Integer size) {
        IPage<SupporterVO> page = supportOrderService.getProjectSupporters(projectId, getCurrentUserId(), current, size);
        return Result.success(page);
    }
}
