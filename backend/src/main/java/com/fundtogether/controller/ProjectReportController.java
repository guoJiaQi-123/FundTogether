package com.fundtogether.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fundtogether.common.Result;
import com.fundtogether.entity.ProjectReport;
import com.fundtogether.security.LoginUser;
import com.fundtogether.service.ProjectReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 项目举报控制器
 * <p>
 * 提供项目举报相关的API功能，包括：
 * <ul>
 *   <li>提交举报：登录用户对项目提交举报，需提供举报原因</li>
 *   <li>查询我的举报：分页查询当前用户提交的举报记录</li>
 *   <li>管理员查询所有举报：管理员分页查询所有举报记录，支持按状态筛选</li>
 *   <li>管理员处理举报：管理员审核处理举报，记录处理结果</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/report")
public class ProjectReportController {

    /** 项目举报服务，处理举报的提交、查询与审核 */
    @Autowired
    private ProjectReportService projectReportService;

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
     * 提交项目举报
     *
     * @param projectId 项目ID
     * @param params    请求体，包含reason（举报原因）
     * @return 举报提交结果提示
     */
    @PostMapping("/{projectId}")
    public Result<?> submitReport(@PathVariable Long projectId, @RequestBody Map<String, String> params) {
        String reason = params.get("reason");
        projectReportService.submitReport(getCurrentUserId(), projectId, reason);
        return Result.success("举报已提交");
    }

    /**
     * 分页查询当前用户提交的举报记录
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @return 分页举报记录列表
     */
    @GetMapping("/my-list")
    public Result<IPage<ProjectReport>> getMyReports(@RequestParam(defaultValue = "1") Integer current,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(projectReportService.getMyReports(getCurrentUserId(), current, size));
    }

    /**
     * 管理员分页查询所有举报记录（仅管理员）
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @param status  举报状态，可选，用于筛选
     * @return 分页举报记录列表
     */
    @GetMapping("/admin/list")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<IPage<ProjectReport>> getAllReports(@RequestParam(defaultValue = "1") Integer current,
                                                       @RequestParam(defaultValue = "10") Integer size,
                                                       @RequestParam(required = false) Integer status) {
        return Result.success(projectReportService.getAllReports(current, size, status));
    }

    /**
     * 管理员处理举报（仅管理员）
     *
     * @param id     举报记录ID
     * @param params 请求体，包含handleResult（处理结果说明）
     * @return 处理结果提示
     */
    @PutMapping("/admin/handle/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> handleReport(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String handleResult = params.get("handleResult");
        LoginUser admin = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        projectReportService.handleReport(id, admin.getId(), handleResult);
        return Result.success("举报已处理");
    }
}
