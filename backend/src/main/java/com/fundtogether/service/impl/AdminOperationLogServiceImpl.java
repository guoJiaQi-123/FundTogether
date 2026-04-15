package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.entity.AdminOperationLog;
import com.fundtogether.mapper.AdminOperationLogMapper;
import com.fundtogether.service.AdminOperationLogService;
import org.springframework.stereotype.Service;

@Service
public class AdminOperationLogServiceImpl extends ServiceImpl<AdminOperationLogMapper, AdminOperationLog> implements AdminOperationLogService {

    @Override
    public void log(Long adminId, String adminName, String operation, String target, String detail, String ip) {
        AdminOperationLog log = new AdminOperationLog();
        log.setAdminId(adminId);
        log.setAdminName(adminName);
        log.setOperation(operation);
        log.setTarget(target);
        log.setDetail(detail);
        log.setIp(ip);
        this.save(log);
    }
}
