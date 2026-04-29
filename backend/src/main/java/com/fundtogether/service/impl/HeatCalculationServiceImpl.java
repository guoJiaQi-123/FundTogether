package com.fundtogether.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fundtogether.entity.HeatRule;
import com.fundtogether.entity.Project;
import com.fundtogether.entity.UserComment;
import com.fundtogether.entity.UserFavorite;
import com.fundtogether.mapper.UserCommentMapper;
import com.fundtogether.mapper.UserFavoriteMapper;
import com.fundtogether.service.HeatCalculationService;
import com.fundtogether.service.HeatRuleService;
import com.fundtogether.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HeatCalculationServiceImpl implements HeatCalculationService {

    @Autowired
    private HeatRuleService heatRuleService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private UserCommentMapper userCommentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateAllProjectHeat() {
        List<Project> projects = projectService.list();
        doRecalculate(projects);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int recalculateActiveProjectHeat() {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getStatus, 1);
        List<Project> projects = projectService.list(wrapper);
        doRecalculate(projects);
        return projects.size();
    }

    private void doRecalculate(List<Project> projects) {
        if (projects.isEmpty()) return;

        List<HeatRule> rules = heatRuleService.listEnabledRules();
        Map<String, BigDecimal> ruleMap = rules.stream()
                .collect(Collectors.toMap(HeatRule::getFactorKey, HeatRule::getWeight));

        List<Long> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());

        Map<Long, Long> favoriteCountMap = countFavorites(projectIds);
        Map<Long, Long> commentCountMap = countComments(projectIds);

        LocalDateTime now = LocalDateTime.now();
        int updated = 0;

        for (Project project : projects) {
            try {
                int heat = calculateHeat(project, ruleMap, favoriteCountMap, commentCountMap, now);
                project.setHeat(heat);
                projectService.updateById(project);
                updated++;
            } catch (Exception e) {
                log.error("计算项目热度失败, projectId: {}", project.getId(), e);
            }
        }

        log.info("热度重算完成, 共处理{}个项目, 更新{}个", projects.size(), updated);
    }

    private int calculateHeat(Project project,
                              Map<String, BigDecimal> ruleMap,
                              Map<Long, Long> favoriteCountMap,
                              Map<Long, Long> commentCountMap,
                              LocalDateTime now) {
        BigDecimal score = BigDecimal.ZERO;

        BigDecimal wSupporter = ruleMap.getOrDefault("supporter_count", BigDecimal.ZERO);
        BigDecimal wFunding = ruleMap.getOrDefault("funding_amount", BigDecimal.ZERO);
        BigDecimal wProgress = ruleMap.getOrDefault("funding_progress", BigDecimal.ZERO);
        BigDecimal wFavorite = ruleMap.getOrDefault("favorite_count", BigDecimal.ZERO);
        BigDecimal wComment = ruleMap.getOrDefault("comment_count", BigDecimal.ZERO);
        BigDecimal wDecay = ruleMap.getOrDefault("time_decay", BigDecimal.ZERO);

        int supporterCount = project.getSupporterCount() != null ? project.getSupporterCount() : 0;
        score = score.add(wSupporter.multiply(BigDecimal.valueOf(supporterCount)));

        BigDecimal fundingAmount = project.getCurrentAmount() != null ? project.getCurrentAmount() : BigDecimal.ZERO;
        score = score.add(wFunding.multiply(fundingAmount.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)));

        if (project.getTargetAmount() != null && project.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal progress = fundingAmount.multiply(BigDecimal.valueOf(100))
                    .divide(project.getTargetAmount(), 2, RoundingMode.HALF_UP);
            score = score.add(wProgress.multiply(progress));
        }

        Long favCount = favoriteCountMap.getOrDefault(project.getId(), 0L);
        score = score.add(wFavorite.multiply(BigDecimal.valueOf(favCount)));

        Long cmtCount = commentCountMap.getOrDefault(project.getId(), 0L);
        score = score.add(wComment.multiply(BigDecimal.valueOf(cmtCount)));

        if (project.getStartTime() != null) {
            long daysSinceStart = ChronoUnit.DAYS.between(project.getStartTime(), now);
            if (daysSinceStart < 0) daysSinceStart = 0;
            BigDecimal decayScore = BigDecimal.valueOf(100)
                    .divide(BigDecimal.valueOf(1 + daysSinceStart), 2, RoundingMode.HALF_UP);
            score = score.add(wDecay.multiply(decayScore));
        }

        return score.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private Map<Long, Long> countFavorites(List<Long> projectIds) {
        if (projectIds.isEmpty()) return Map.of();
        QueryWrapper<UserFavorite> qw = new QueryWrapper<>();
        qw.select("project_id", "COUNT(*) as cnt")
                .in("project_id", projectIds)
                .eq("deleted", 0)
                .groupBy("project_id");
        List<Map<String, Object>> results = userFavoriteMapper.selectMaps(qw);
        return results.stream().collect(Collectors.toMap(
                m -> ((Number) m.get("project_id")).longValue(),
                m -> ((Number) m.get("cnt")).longValue()
        ));
    }

    private Map<Long, Long> countComments(List<Long> projectIds) {
        if (projectIds.isEmpty()) return Map.of();
        QueryWrapper<UserComment> qw = new QueryWrapper<>();
        qw.select("project_id", "COUNT(*) as cnt")
                .in("project_id", projectIds)
                .eq("deleted", 0)
                .groupBy("project_id");
        List<Map<String, Object>> results = userCommentMapper.selectMaps(qw);
        return results.stream().collect(Collectors.toMap(
                m -> ((Number) m.get("project_id")).longValue(),
                m -> ((Number) m.get("cnt")).longValue()
        ));
    }
}
