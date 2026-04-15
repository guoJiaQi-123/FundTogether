package com.fundtogether.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fundtogether.entity.ProjectReport;

public interface ProjectReportService extends IService<ProjectReport> {
    void submitReport(Long reporterId, Long projectId, String reason);
    IPage<ProjectReport> getMyReports(Long reporterId, Integer current, Integer size);
    IPage<ProjectReport> getAllReports(Integer current, Integer size, Integer status);
    void handleReport(Long id, Long handlerId, String handleResult);
}
