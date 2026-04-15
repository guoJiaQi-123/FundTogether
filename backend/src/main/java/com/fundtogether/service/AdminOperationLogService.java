package com.fundtogether.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fundtogether.entity.AdminOperationLog;

public interface AdminOperationLogService extends IService<AdminOperationLog> {
    void log(Long adminId, String adminName, String operation, String target, String detail, String ip);
}
