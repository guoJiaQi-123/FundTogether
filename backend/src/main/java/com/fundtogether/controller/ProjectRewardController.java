package com.fundtogether.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fundtogether.common.Result;
import com.fundtogether.entity.ProjectReward;
import com.fundtogether.service.ProjectRewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目回报控制器
 * <p>
 * 提供项目回报（众筹回报档位）的增删改查API功能，包括：
 * <ul>
 *   <li>创建回报：为指定项目添加回报档位</li>
 *   <li>查询回报列表：查询指定项目的所有回报档位，按金额升序排列</li>
 *   <li>更新回报：修改指定回报档位的信息</li>
 *   <li>删除回报：删除指定回报档位</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/projects/{projectId}/rewards")
public class ProjectRewardController {

    /** 项目回报服务，处理回报档位的增删改查 */
    @Autowired
    private ProjectRewardService projectRewardService;

    /**
     * 创建项目回报档位
     *
     * @param projectId 项目ID
     * @param reward    回报实体，包含名称、金额、描述、限量数量等
     * @return 创建结果提示
     */
    @PostMapping
    public Result<?> createReward(@PathVariable Long projectId, @RequestBody ProjectReward reward) {
        reward.setProjectId(projectId);
        if (reward.getCurrentCount() == null) {
            reward.setCurrentCount(0); // 默认已认领数量为0
        }
        projectRewardService.save(reward);
        return Result.success("回报创建成功");
    }

    /**
     * 查询指定项目的回报档位列表
     * <p>
     * 按回报金额升序排列，便于前端按金额从小到大展示。
     * </p>
     *
     * @param projectId 项目ID
     * @return 回报档位列表
     */
    @GetMapping
    public Result<List<ProjectReward>> getRewardsByProject(@PathVariable Long projectId) {
        LambdaQueryWrapper<ProjectReward> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectReward::getProjectId, projectId)
               .orderByAsc(ProjectReward::getAmount);
        List<ProjectReward> rewards = projectRewardService.list(wrapper);
        return Result.success(rewards);
    }

    /**
     * 更新项目回报档位
     * <p>
     * 校验回报是否属于指定项目，防止跨项目修改。
     * </p>
     *
     * @param projectId 项目ID
     * @param id        回报ID
     * @param reward    回报实体，包含待更新的字段
     * @return 更新结果提示
     */
    @PutMapping("/{id}")
    public Result<?> updateReward(@PathVariable Long projectId, @PathVariable Long id, @RequestBody ProjectReward reward) {
        ProjectReward existing = projectRewardService.getById(id);
        if (existing == null || !existing.getProjectId().equals(projectId)) {
            return Result.error("回报不存在");
        }
        reward.setId(id);
        reward.setProjectId(projectId);
        projectRewardService.updateById(reward);
        return Result.success("回报更新成功");
    }

    /**
     * 删除项目回报档位
     * <p>
     * 校验回报是否属于指定项目，防止跨项目删除。
     * </p>
     *
     * @param projectId 项目ID
     * @param id        回报ID
     * @return 删除结果提示
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteReward(@PathVariable Long projectId, @PathVariable Long id) {
        ProjectReward existing = projectRewardService.getById(id);
        if (existing == null || !existing.getProjectId().equals(projectId)) {
            return Result.error("回报不存在");
        }
        projectRewardService.removeById(id);
        return Result.success("回报删除成功");
    }
}
