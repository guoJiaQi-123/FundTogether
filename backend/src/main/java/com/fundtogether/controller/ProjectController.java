package com.fundtogether.controller;

import com.fundtogether.common.Result;
import com.fundtogether.entity.Project;
import com.fundtogether.entity.ProjectRecommend;
import com.fundtogether.service.ProjectRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目推荐控制器
 * <p>
 * 提供项目推荐相关的公开API功能，包括：
 * <ul>
 *   <li>获取推荐项目列表：查询首页展示的推荐项目，支持限制返回数量</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    /** 项目推荐服务，用于查询推荐项目及其关联的项目信息 */
    @Autowired
    private ProjectRecommendService projectRecommendService;

    /**
     * 获取推荐项目列表
     * <p>
     * 查询当前有效的推荐项目，将推荐记录转换为项目实体返回。
     * 仅返回推荐记录中关联的项目基本信息（标题、封面、摘要、金额、支持人数等）。
     * </p>
     *
     * @param limit 返回数量上限，默认5
     * @return 推荐项目列表
     */
    @GetMapping("/recommend")
    public Result<List<Project>> getRecommendProjects(@RequestParam(defaultValue = "5") Integer limit) {
        List<ProjectRecommend> recommends = projectRecommendService.listActiveWithProjectInfo(limit);
        // 将推荐记录转换为项目实体，仅保留前端展示所需的字段
        List<Project> projects = recommends.stream().map(rec -> {
            Project p = new Project();
            p.setId(rec.getProjectId());
            p.setTitle(rec.getProjectTitle());
            p.setCoverImage(rec.getCoverImage());
            p.setSummary(rec.getSummary());
            p.setTargetAmount(rec.getTargetAmount());
            p.setCurrentAmount(rec.getCurrentAmount());
            p.setSupporterCount(rec.getSupporterCount());
            p.setStatus(rec.getStatus());
            p.setEndTime(rec.getEndTime());
            return p;
        }).collect(Collectors.toList());

        return Result.success(projects);
    }
}
