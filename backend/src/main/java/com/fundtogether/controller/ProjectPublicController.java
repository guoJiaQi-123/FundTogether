package com.fundtogether.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fundtogether.common.Result;
import com.fundtogether.entity.Project;
import com.fundtogether.entity.SupportOrder;
import com.fundtogether.service.ProjectService;
import com.fundtogether.service.SupportOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 项目公开接口控制器
 * <p>
 * 提供无需登录即可访问的项目公开API功能，包括：
 * <ul>
 *   <li>项目列表查询：支持分类、金额范围、进度范围、关键词、排序等多维度筛选</li>
 *   <li>项目详情查询：获取指定项目的详细信息</li>
 *   <li>筹款进度查询：获取指定项目按天统计的累计筹款金额数据，用于绘制进度图表</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/public/project")
public class ProjectPublicController {

    /** 项目服务，处理项目列表查询和详情查询 */
    @Autowired
    private ProjectService projectService;

    /** 支持订单服务，用于查询项目筹款进度数据 */
    @Autowired
    private SupportOrderService supportOrderService;

    /**
     * 分页查询在线项目列表
     * <p>
     * 支持多维度筛选条件：分类、金额范围、进度范围、关键词搜索、排序方式。
     * </p>
     *
     * @param current      当前页码，默认1
     * @param size         每页条数，默认10
     * @param categoryId   分类ID，可选
     * @param minAmount    目标金额下限，可选
     * @param maxAmount    目标金额上限，可选
     * @param minProgress  筹款进度下限（百分比），可选
     * @param maxProgress  筹款进度上限（百分比），可选
     * @param keyword      搜索关键词，可选
     * @param sortType     排序类型，可选
     * @return 分页在线项目列表
     */
    @GetMapping("/list")
    public Result<IPage<Project>> getProjectList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) Double minProgress,
            @RequestParam(required = false) Double maxProgress,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer sortType) {
        IPage<Project> page = projectService.getOnlineProjects(current, size, categoryId, minAmount, maxAmount, minProgress, maxProgress, keyword, sortType);
        return Result.success(page);
    }

    /**
     * 查询项目详情
     *
     * @param id 项目ID
     * @return 项目详细信息
     */
    @GetMapping("/{id}")
    public Result<Project> getProjectDetail(@PathVariable Long id) {
        Project project = projectService.getProjectDetail(id);
        return Result.success(project);
    }

    /**
     * 查询项目筹款进度数据
     * <p>
     * 按天统计项目从开始日期到今天的累计筹款金额，
     * 返回每日的日期和累计金额数据，用于前端绘制筹款进度折线图。
     * </p>
     *
     * @param id 项目ID
     * @return 筹款进度数据列表，每项包含date（日期）和amount（累计金额）
     */
    @GetMapping("/{id}/funding-progress")
    public Result<?> getProjectFundingProgress(@PathVariable Long id) {
        Project project = projectService.getById(id);
        if (project == null) {
            return Result.error("项目不存在");
        }

        // 查询该项目所有已支付的支持订单
        QueryWrapper<SupportOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("project_id", id)
               .eq("status", 1) // 1-已支付
               .orderByAsc("pay_time");
        List<SupportOrder> orders = supportOrderService.list(wrapper);

        // 确定统计起始日期：取项目开始日期与最早支付日期中较早者
        LocalDate startDate = project.getStartTime() != null ? project.getStartTime().toLocalDate() : LocalDate.now().minusDays(14);
        if (!orders.isEmpty() && orders.get(0).getPayTime().toLocalDate().isBefore(startDate)) {
            startDate = orders.get(0).getPayTime().toLocalDate();
        }

        LocalDate today = LocalDate.now();
        if (startDate.isAfter(today)) {
            startDate = today;
        }

        // 按天统计每日新增筹款金额
        Map<LocalDate, BigDecimal> dailyIncrease = new HashMap<>();
        for (SupportOrder order : orders) {
            if (order.getPayTime() == null) continue;
            LocalDate d = order.getPayTime().toLocalDate();
            dailyIncrease.put(d, dailyIncrease.getOrDefault(d, BigDecimal.ZERO).add(order.getAmount()));
        }

        List<Map<String, Object>> progress = new ArrayList<>();
        BigDecimal currentCumulative = BigDecimal.ZERO;

        // 生成从起始日期到今天的每一天的累计数据
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(today)) {
            BigDecimal inc = dailyIncrease.getOrDefault(currentDate, BigDecimal.ZERO);
            currentCumulative = currentCumulative.add(inc);

            Map<String, Object> entry = new HashMap<>();
            entry.put("date", currentDate.toString());
            entry.put("amount", currentCumulative);
            progress.add(entry);

            currentDate = currentDate.plusDays(1);
        }

        return Result.success(progress);
    }
}
