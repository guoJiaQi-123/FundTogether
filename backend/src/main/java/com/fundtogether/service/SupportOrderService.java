package com.fundtogether.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fundtogether.dto.SupportOrderCreateDTO;
import com.fundtogether.entity.SupportOrder;
import com.fundtogether.vo.SupporterVO;

import java.util.Map;

public interface SupportOrderService extends IService<SupportOrder> {
    void createOrder(SupportOrderCreateDTO dto, Long userId);
    IPage<SupportOrder> getMyOrders(Long userId, Integer current, Integer size);
    IPage<SupporterVO> getProjectSupporters(Long projectId, Long sponsorId, Integer current, Integer size);
    Map<String, Object> getMyStats(Long userId);
    com.fundtogether.entity.Project getProjectByOrderId(Long orderId);
}
