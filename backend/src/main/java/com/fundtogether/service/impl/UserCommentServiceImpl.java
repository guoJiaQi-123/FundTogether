package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.common.enums.UserRole;
import com.fundtogether.entity.Project;
import com.fundtogether.entity.SysUser;
import com.fundtogether.entity.UserComment;
import com.fundtogether.mapper.UserCommentMapper;
import com.fundtogether.service.ProjectService;
import com.fundtogether.service.SysUserService;
import com.fundtogether.service.UserCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCommentServiceImpl extends ServiceImpl<UserCommentMapper, UserComment> implements UserCommentService {

    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private ProjectService projectService;

    @Override
    public void addComment(Long userId, Long projectId, Long parentId, String content) {
        UserComment comment = new UserComment();
        comment.setUserId(userId);
        comment.setProjectId(projectId);
        comment.setParentId(parentId != null ? parentId : 0L);
        comment.setContent(content);
        comment.setLikesCount(0);
        comment.setStatus(1); // 1-正常
        this.save(comment);
    }

    @Override
    public IPage<UserComment> getProjectComments(Long projectId, Integer current, Integer size) {
        Page<UserComment> page = new Page<>(current, size);
        LambdaQueryWrapper<UserComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserComment::getProjectId, projectId);
        wrapper.eq(UserComment::getStatus, 1);
        wrapper.orderByDesc(UserComment::getCreatedAt);
        
        IPage<UserComment> result = this.page(page, wrapper);
        List<UserComment> records = result.getRecords();
        for (UserComment comment : records) {
            SysUser user = sysUserService.getById(comment.getUserId());
            if (user != null) {
                comment.setNickname(user.getNickname() != null ? user.getNickname() : user.getAccount());
                comment.setAvatar(user.getAvatar());
            }
        }
        
        return result;
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        UserComment comment = this.getById(commentId);
        if (comment == null) return;
        
        Project project = projectService.getById(comment.getProjectId());
        SysUser user = sysUserService.getById(userId);
        
        // 发起人、管理员、自己可以删除
        if (comment.getUserId().equals(userId) || 
            (project != null && project.getSponsorId().equals(userId)) || 
            (user != null && UserRole.isAdmin(user.getRole()))) {
            comment.setStatus(0);
            this.updateById(comment);
        } else {
            throw new RuntimeException("无权删除此评论");
        }
    }

    @Override
    public void likeComment(Long commentId, Long userId) {
        UserComment comment = this.getById(commentId);
        if (comment != null) {
            comment.setLikesCount(comment.getLikesCount() + 1);
            this.updateById(comment);
        }
    }
}