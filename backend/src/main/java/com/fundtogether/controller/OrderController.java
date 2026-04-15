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

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private SupportOrderService supportOrderService;

    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getId();
    }

    @PostMapping("/create")
    @RateLimit(permits = 3, seconds = 5, key = "order_create")
    public Result<?> createOrder(@RequestBody @Valid SupportOrderCreateDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        supportOrderService.createOrder(dto, getCurrentUserId());
        return Result.success("支持订单创建成功");
    }

    @GetMapping("/my-list")
    public Result<IPage<SupportOrder>> getMyOrders(@RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<SupportOrder> page = supportOrderService.getMyOrders(getCurrentUserId(), current, size);
        return Result.success(page);
    }

    @GetMapping("/my-stats")
    public Result<?> getMyStats() {
        return Result.success(supportOrderService.getMyStats(getCurrentUserId()));
    }

    @PostMapping("/delivery/{id}")
    public Result<?> updateDeliveryStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> params) {
        String expressNo = params.get("expressNo");
        SupportOrder order = supportOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }

        com.fundtogether.entity.Project project = supportOrderService.getProjectByOrderId(order.getId());
        if (project == null || !project.getSponsorId().equals(getCurrentUserId())) {
            return Result.error(403, "无权修改该订单发货状态");
        }

        order.setDeliveryStatus(1);
        order.setExpressNo(expressNo);
        order.setDeliveryTime(java.time.LocalDateTime.now());

        supportOrderService.updateById(order);
        return Result.success("发货状态更新成功");
    }
}
