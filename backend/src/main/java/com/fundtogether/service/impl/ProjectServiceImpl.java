package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fundtogether.dto.ProjectCreateDTO;
import com.fundtogether.dto.ProjectUpdateDTO;
import com.fundtogether.entity.Project;
import com.fundtogether.entity.SysUser;
import com.fundtogether.mapper.ProjectMapper;
import com.fundtogether.service.ProjectService;
import com.fundtogether.service.SysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @org.springframework.beans.factory.annotation.Autowired
    private SysUserService sysUserService;

    private void fillSponsorInfo(List<Project> projects) {
        if (projects == null || projects.isEmpty()) return;
        Set<Long> sponsorIds = projects.stream()
                .map(Project::getSponsorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (sponsorIds.isEmpty()) return;
        Map<Long, SysUser> userMap = sysUserService.listByIds(sponsorIds)
                .stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        for (Project p : projects) {
            SysUser sponsor = userMap.get(p.getSponsorId());
            if (sponsor != null) {
                p.setSponsorName(sponsor.getNickname());
                p.setSponsorAvatar(sponsor.getAvatar());
            }
        }
    }

    @Override
    public IPage<Project> getOnlineProjects(Integer current, Integer size, Long categoryId, Double minAmount, Double maxAmount, Double minProgress, Double maxProgress, String keyword, Integer sortType) {
        Page<Project> page = new Page<>(current, size);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        // 状态1表示筹款中
        wrapper.eq(Project::getStatus, 1);
        
        if (categoryId != null) {
            wrapper.eq(Project::getCategoryId, categoryId);
        }
        
        if (minAmount != null) {
            wrapper.ge(Project::getTargetAmount, minAmount);
        }
        if (maxAmount != null) {
            wrapper.le(Project::getTargetAmount, maxAmount);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Project::getTitle, keyword).or().like(Project::getSummary, keyword));
        }
        
        if (sortType != null) {
            if (sortType == 1) { // 按热度降序
                wrapper.orderByDesc(Project::getHeat);
            } else if (sortType == 2) { // 按最新发布降序
                wrapper.orderByDesc(Project::getStartTime);
            } else if (sortType == 3) { // 按目标金额降序
                wrapper.orderByDesc(Project::getTargetAmount);
            }
        } else {
            wrapper.orderByDesc(Project::getHeat); // 默认热度降序
        }
        
        // Progress filtering is tricky in SQL because it involves current_amount / target_amount.
        // We can do it via custom SQL or apply simple math if needed, but for simplicity we'll do it via SQL.
        // mybatis-plus supports apply for custom SQL
        if (minProgress != null) {
            wrapper.apply("current_amount / target_amount * 100 >= {0}", minProgress);
        }
        if (maxProgress != null) {
            wrapper.apply("current_amount / target_amount * 100 <= {0}", maxProgress);
        }
        
        IPage<Project> result = this.page(page, wrapper);
        fillSponsorInfo(result.getRecords());
        return result;
    }

    @Override
    public Project getProjectDetail(Long id) {
        Project project = this.getById(id);
        if (project == null || project.getStatus() == 4) {
            throw new RuntimeException("项目不存在或已下架");
        }
        fillSponsorInfo(List.of(project));
        return project;
    }

    @Override
    public void createProject(ProjectCreateDTO dto, Long userId) {
        Project project = new Project();
        BeanUtils.copyProperties(dto, project);
        project.setSponsorId(userId);
        project.setStatus(0); // 0-待审核
        project.setCurrentAmount(BigDecimal.ZERO);
        project.setSupporterCount(0);
        project.setHeat(0);
        this.save(project);
    }

    @Override
    public void updatePendingProject(ProjectUpdateDTO dto, Long userId) {
        Project project = this.getById(dto.getId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        if (!project.getSponsorId().equals(userId)) {
            throw new RuntimeException("无权修改他人项目");
        }
        if (project.getStatus() != 0) { // 必须是待审核
            throw new RuntimeException("只有待审核的项目才能修改");
        }
        
        BeanUtils.copyProperties(dto, project);
        this.updateById(project);
    }

    @Override
    public void updateActiveProject(ProjectUpdateDTO dto, Long userId) {
        Project project = this.getById(dto.getId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        if (!project.getSponsorId().equals(userId)) {
            throw new RuntimeException("无权修改他人项目");
        }
        if (project.getStatus() != 1) { // 必须是筹款中
            throw new RuntimeException("只有筹款中的项目才能修改非核心信息");
        }
        
        // 只能修改非核心信息：简介、内容、视频
        project.setSummary(dto.getSummary());
        project.setContent(dto.getContent());
        project.setVideoUrl(dto.getVideoUrl());
        this.updateById(project);
    }

    @Override
    public void cancelPendingProject(Long projectId, Long userId) {
        Project project = this.getById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        if (!project.getSponsorId().equals(userId)) {
            throw new RuntimeException("无权取消他人项目");
        }
        if (project.getStatus() != 0) { // 0-待审核
            throw new RuntimeException("只有待审核的项目才能取消");
        }
        
        project.setStatus(3); // 3-已取消
        this.updateById(project);
    }

    @Override
    public IPage<Project> getPendingProjects(Integer current, Integer size) {
        Page<Project> page = new Page<>(current, size);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getStatus, 0); // 0-待审核
        wrapper.orderByAsc(Project::getCreatedAt); // 按提交时间排序
        
        return this.page(page, wrapper);
    }

    @Override
    public void approveProject(Long projectId) {
        Project project = this.getById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        if (project.getStatus() != 0) { // 0-待审核
            throw new RuntimeException("只有待审核的项目才能通过审核");
        }
        
        project.setStatus(1); // 1-筹款中
        project.setStartTime(LocalDateTime.now());
        this.updateById(project);
    }

    @Override
    public void rejectProject(Long projectId, String reason) {
        Project project = this.getById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        if (project.getStatus() != 0) { // 0-待审核
            throw new RuntimeException("只有待审核的项目才能被驳回");
        }
        
        project.setStatus(2); // 2-已驳回
        project.setAuditReason(reason);
        this.updateById(project);
    }

    @Override
    public void takedownProject(Long projectId, String reason) {
        Project project = this.getById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        if (project.getStatus() != 1) { // 1-筹款中
            throw new RuntimeException("只有筹款中的项目才能下架");
        }
        
        project.setStatus(4); // 4-已下架
        project.setAuditReason(reason);
        this.updateById(project);
    }
}
