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

@RestController
@RequestMapping("/api/report")
public class ProjectReportController {

    @Autowired
    private ProjectReportService projectReportService;

    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getId();
    }

    @PostMapping("/{projectId}")
    public Result<?> submitReport(@PathVariable Long projectId, @RequestBody Map<String, String> params) {
        String reason = params.get("reason");
        projectReportService.submitReport(getCurrentUserId(), projectId, reason);
        return Result.success("举报已提交");
    }

    @GetMapping("/my-list")
    public Result<IPage<ProjectReport>> getMyReports(@RequestParam(defaultValue = "1") Integer current,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(projectReportService.getMyReports(getCurrentUserId(), current, size));
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<IPage<ProjectReport>> getAllReports(@RequestParam(defaultValue = "1") Integer current,
                                                       @RequestParam(defaultValue = "10") Integer size,
                                                       @RequestParam(required = false) Integer status) {
        return Result.success(projectReportService.getAllReports(current, size, status));
    }

    @PutMapping("/admin/handle/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Result<?> handleReport(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String handleResult = params.get("handleResult");
        LoginUser admin = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        projectReportService.handleReport(id, admin.getId(), handleResult);
        return Result.success("举报已处理");
    }
}
