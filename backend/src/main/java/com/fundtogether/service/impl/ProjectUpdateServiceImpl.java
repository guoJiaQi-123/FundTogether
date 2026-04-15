package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.common.enums.MessageType;
import com.fundtogether.common.exception.BusinessException;
import com.fundtogether.entity.Project;
import com.fundtogether.entity.ProjectUpdate;
import com.fundtogether.entity.SupportOrder;
import com.fundtogether.entity.SysUserMessage;
import com.fundtogether.mapper.ProjectMapper;
import com.fundtogether.mapper.ProjectUpdateMapper;
import com.fundtogether.service.ProjectUpdateService;
import com.fundtogether.service.SupportOrderService;
import com.fundtogether.service.SysUserMessageService;
import com.fundtogether.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectUpdateServiceImpl extends ServiceImpl<ProjectUpdateMapper, ProjectUpdate> implements ProjectUpdateService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private SupportOrderService supportOrderService;

    @Autowired
    private SysUserMessageService sysUserMessageService;

    @Override
    public void publishUpdate(Long sponsorId, Long projectId, String title, String content, String images) {
        Project project = projectMapper.selectById(projectId);
        if (project == null || !project.getSponsorId().equals(sponsorId)) {
            throw new BusinessException("无权操作该项目");
        }

        ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);
        update.setTitle(title);
        update.setContent(content);
        update.setImages(images);
        this.save(update);

        notifySupporters(project, update);
    }

    private void notifySupporters(Project project, ProjectUpdate update) {
        LambdaQueryWrapper<SupportOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SupportOrder::getProjectId, project.getId());
        wrapper.eq(SupportOrder::getStatus, 1);
        List<SupportOrder> orders = supportOrderService.list(wrapper);

        List<Long> supporterIds = orders.stream()
                .map(SupportOrder::getUserId)
                .distinct()
                .collect(Collectors.toList());

        if (!supporterIds.isEmpty()) {
            List<SysUserMessage> messages = supporterIds.stream().map(userId -> {
                SysUserMessage msg = new SysUserMessage();
                msg.setUserId(userId);
                msg.setType(MessageType.PROJECT_UPDATE.getCode());
                msg.setTitle("项目动态更新: " + project.getTitle());
                msg.setContent(update.getTitle() + " - " + (update.getContent() != null && update.getContent().length() > 100
                        ? update.getContent().substring(0, 100) + "..." : update.getContent()));
                msg.setRelatedId(project.getId());
                msg.setIsRead(0);
                return msg;
            }).collect(Collectors.toList());

            sysUserMessageService.saveBatch(messages);

            String wsMessage = String.format("{\"type\":\"PROJECT_UPDATE\",\"projectId\":%d,\"title\":\"%s\"}",
                    project.getId(), update.getTitle());
            for (Long supporterId : supporterIds) {
                WebSocketServer.sendMessageToUser(String.valueOf(supporterId), wsMessage);
            }
        }
    }

    @Override
    public List<ProjectUpdate> getProjectUpdates(Long projectId) {
        return this.list(new LambdaQueryWrapper<ProjectUpdate>()
                .eq(ProjectUpdate::getProjectId, projectId)
                .orderByDesc(ProjectUpdate::getCreatedAt));
    }
}
