package com.fundtogether.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fundtogether.dto.SupportOrderCreateDTO;
import com.fundtogether.entity.SupportOrder;
import com.fundtogether.vo.SupporterVO;

import java.util.Map;
import java.util.List;

public interface SupportOrderService extends IService<SupportOrder> {
    void createOrder(SupportOrderCreateDTO dto, Long userId);
    IPage<SupportOrder> getMyOrders(Long userId, Integer current, Integer size, Integer status);
    IPage<SupporterVO> getProjectSupporters(Long projectId, Long sponsorId, Integer current, Integer size);
    Map<String, Object> getMyStats(Long userId);
    com.fundtogether.entity.Project getProjectByOrderId(Long orderId);
    List<Map<String, Object>> getUserMonthlyTrend(Long userId);
    List<Map<String, Object>> getUserProjectDistribution(Long userId);
}
