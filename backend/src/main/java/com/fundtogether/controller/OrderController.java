package com.fundtogether.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fundtogether.common.Result;
import com.fundtogether.common.annotation.RateLimit;
import com.fundtogether.dto.SupportOrderCreateDTO;
import com.fundtogether.entity.SupportOrder;
import com.fundtogether.security.LoginUser;
import com.fundtogether.service.SupportOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 * <p>
 * 提供众筹支持订单相关的API功能，包括：
 * <ul>
 *   <li>创建订单：用户对项目发起支持，创建支持订单（限流保护）</li>
 *   <li>查询我的订单：分页查询当前用户的支持订单列表</li>
 *   <li>查询我的统计：获取当前用户的支持统计信息</li>
 *   <li>更新发货状态：项目发起人填写快递单号，标记订单为已发货</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    /** 支持订单服务，处理订单的创建、查询及发货状态更新 */
    @Autowired
    private SupportOrderService supportOrderService;

    /**
     * 获取当前登录用户ID
     *
     * @return 当前登录用户的ID
     */
    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getId();
    }

    /**
     * 创建支持订单
     * <p>
     * 用户对指定项目发起支持，创建支持订单。接口限流：5秒内最多3次请求。
     * </p>
     *
     * @param dto           订单创建DTO，包含projectId、amount、message、rewardId等
     * @param bindingResult 参数校验结果
     * @return 订单创建结果提示
     */
    @PostMapping("/create")
    @RateLimit(permits = 3, seconds = 5, key = "order_create")
    public Result<?> createOrder(@RequestBody @Valid SupportOrderCreateDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        supportOrderService.createOrder(dto, getCurrentUserId());
        return Result.success("支持订单创建成功");
    }

    /**
     * 分页查询当前用户的支持订单列表
     *
     * @param current 当前页码，默认1
     * @param size    每页条数，默认10
     * @param status  订单状态筛选，可选
     * @return 分页支持订单列表
     */
    @GetMapping("/my-list")
    public Result<IPage<SupportOrder>> getMyOrders(@RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        IPage<SupportOrder> page = supportOrderService.getMyOrders(getCurrentUserId(), current, size, status);
        return Result.success(page);
    }

    /**
     * 查询当前用户的支持统计信息
     *
     * @return 用户支持统计数据
     */
    @GetMapping("/my-stats")
    public Result<?> getMyStats() {
        return Result.success(supportOrderService.getMyStats(getCurrentUserId()));
    }

    /**
     * 更新订单发货状态
     * <p>
     * 项目发起人填写快递单号，将订单标记为已发货。
     * 仅项目发起人有权操作，其他用户返回403。
     * </p>
     *
     * @param id     订单ID
     * @param params 请求体，包含expressNo（快递单号）
     * @return 发货状态更新结果提示
     */
    @PostMapping("/delivery/{id}")
    public Result<?> updateDeliveryStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> params) {
        String expressNo = params.get("expressNo");
        SupportOrder order = supportOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 校验当前用户是否为该项目的发起人
        com.fundtogether.entity.Project project = supportOrderService.getProjectByOrderId(order.getId());
        if (project == null || !project.getSponsorId().equals(getCurrentUserId())) {
            return Result.error(403, "无权修改该订单发货状态");
        }

        // 更新发货状态和快递信息
        order.setDeliveryStatus(1); // 1-已发货
        order.setExpressNo(expressNo);
        order.setDeliveryTime(java.time.LocalDateTime.now());

        supportOrderService.updateById(order);
        return Result.success("发货状态更新成功");
    }

    /**
     * 获取用户近6个月月度支持趋势
     *
     * @return 用户月度支持趋势数据
     */
    @GetMapping("/my-monthly-trend")
    public Result<?> getMyMonthlyTrend() {
        return Result.success(supportOrderService.getUserMonthlyTrend(getCurrentUserId()));
    }

    /**
     * 获取用户支持项目分布（Top 10）
     *
     * @return 用户支持项目分布数据
     */
    @GetMapping("/my-project-distribution")
    public Result<?> getMyProjectDistribution() {
        return Result.success(supportOrderService.getUserProjectDistribution(getCurrentUserId()));
    }
}
