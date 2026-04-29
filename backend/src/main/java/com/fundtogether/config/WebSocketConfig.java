package com.fundtogether.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket 配置类
 * <p>
 * 用于启用 Spring Boot 对 WebSocket 的支持，注册 {@link ServerEndpointExporter} Bean 后，
 * 使用 {@code @ServerEndpoint} 注解标注的 WebSocket 服务端点才能被自动发现和部署。
 * </p>
 * <p>
 * 当前项目中 WebSocket 主要用于实时通信场景（如众筹项目的实时状态推送、消息通知等），
 * 服务端可通过 WebSocket 主动向客户端推送数据，无需客户端轮询。
 * </p>
 * <p>
 * 注意：如果项目以 WAR 包形式部署到外部 Servlet 容器（如 Tomcat），
 * 则不应注册此 Bean，因为外部容器会自行管理 WebSocket 端点的生命周期，
 * 重复注册会导致异常。当前项目使用 Spring Boot 内嵌 Tomcat，因此需要注册。
 * </p>
 */
@Configuration
public class WebSocketConfig {

    /**
     * 注册 WebSocket 服务端点导出器
     * <p>
     * {@link ServerEndpointExporter} 会自动扫描并注册所有带 {@code @ServerEndpoint} 注解的类，
     * 将其作为 WebSocket 端点发布。没有此 Bean，{@code @ServerEndpoint} 注解将不会生效。
     * </p>
     *
     * @return WebSocket 服务端点导出器实例
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
