package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.common.exception.BusinessException;
import com.fundtogether.entity.Project;
import com.fundtogether.entity.ProjectReport;
import com.fundtogether.entity.SysUser;
import com.fundtogether.mapper.ProjectReportMapper;
import com.fundtogether.service.ProjectReportService;
import com.fundtogether.service.ProjectService;
import com.fundtogether.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProjectReportServiceImpl extends ServiceImpl<ProjectReportMapper, ProjectReport> implements ProjectReportService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SysUserService sysUserService;

    @Override
    public void submitReport(Long reporterId, Long projectId, String reason) {
        Project project = projectService.getById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        LambdaQueryWrapper<ProjectReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectReport::getReporterId, reporterId);
        wrapper.eq(ProjectReport::getProjectId, projectId);
        wrapper.eq(ProjectReport::getStatus, 0);
        if (this.count(wrapper) > 0) {
            throw new BusinessException("您已举报过该项目，请等待处理");
        }

        ProjectReport report = new ProjectReport();
        report.setReporterId(reporterId);
        report.setProjectId(projectId);
        report.setReason(reason);
        report.setStatus(0);
        this.save(report);
    }

    @Override
    public IPage<ProjectReport> getMyReports(Long reporterId, Integer current, Integer size) {
        Page<ProjectReport> page = new Page<>(current, size);
        LambdaQueryWrapper<ProjectReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectReport::getReporterId, reporterId);
        wrapper.orderByDesc(ProjectReport::getCreatedAt);
        IPage<ProjectReport> result = this.page(page, wrapper);
        fillReportInfo(result.getRecords());
        return result;
    }

    @Override
    public IPage<ProjectReport> getAllReports(Integer current, Integer size, Integer status) {
        Page<ProjectReport> page = new Page<>(current, size);
        LambdaQueryWrapper<ProjectReport> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ProjectReport::getStatus, status);
        }
        wrapper.orderByDesc(ProjectReport::getCreatedAt);
        IPage<ProjectReport> result = this.page(page, wrapper);
        fillReportInfo(result.getRecords());
        return result;
    }

    @Override
    public void handleReport(Long id, Long handlerId, String handleResult) {
        ProjectReport report = this.getById(id);
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }
        if (report.getStatus() != 0) {
            throw new BusinessException("该举报已处理");
        }

        report.setStatus(1);
        report.setHandlerId(handlerId);
        report.setHandleResult(handleResult);
        report.setHandleTime(LocalDateTime.now());
        this.updateById(report);
    }

    private void fillReportInfo(java.util.List<ProjectReport> reports) {
        for (ProjectReport report : reports) {
            SysUser reporter = sysUserService.getById(report.getReporterId());
            if (reporter != null) {
                report.setReporterName(reporter.getNickname() != null ? reporter.getNickname() : reporter.getAccount());
            }
            Project project = projectService.getById(report.getProjectId());
            if (project != null) {
                report.setProjectName(project.getTitle());
            }
        }
    }
}
