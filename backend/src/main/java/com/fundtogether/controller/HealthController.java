package com.fundtogether.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 * <p>
 * 提供应用健康检查接口，用于监控系统和负载均衡器检测应用是否正常运行。
 * </p>
 */
@RestController
public class HealthController {

    /**
     * 健康检查接口
     * <p>
     * 返回应用运行状态字符串，供监控系统或负载均衡器判断服务是否可用。
     * </p>
     *
     * @return 应用运行状态提示字符串
     */
    @GetMapping("/health")
    public String healthCheck() {
        return "FundTogether Application is running successfully!";
    }
}
