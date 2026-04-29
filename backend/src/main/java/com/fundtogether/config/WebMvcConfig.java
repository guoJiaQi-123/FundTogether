package com.fundtogether.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置类
 * <p>
 * 实现 {@link WebMvcConfigurer} 接口，自定义 Spring MVC 的行为，当前主要配置：
 * <ul>
 *     <li>静态资源映射 —— 将上传文件的本地存储目录映射为可通过 URL 访问的静态资源</li>
 *     <li>跨域配置 —— 配置全局 CORS 策略，允许前端跨域访问后端接口</li>
 * </ul>
 * </p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理
     * <p>
     * 将 URL 路径 /uploads/** 映射到服务器本地文件系统的 uploads 目录，
     * 使得用户上传的图片、文件等可以通过 HTTP 请求直接访问。
     * <ul>
     *     <li>URL 路径：/uploads/**（如 /uploads/avatar.png）</li>
     *     <li>文件位置：项目运行目录下的 uploads/ 文件夹</li>
     * </ul>
     * </p>
     *
     * @param registry 资源处理注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = "file:" + System.getProperty("user.dir") + "/uploads/"; // 拼接本地文件系统上传目录的绝对路径
        registry.addResourceHandler("/uploads/**") // 匹配所有以 /uploads/ 开头的请求
                .addResourceLocations(uploadPath); // 映射到本地文件系统的 uploads 目录
    }

    /**
     * 配置全局跨域映射
     * <p>
     * 定义 Spring MVC 层面的 CORS 策略，适用于所有 Controller 接口：
     * <ul>
     *     <li>允许所有来源（allowedOriginPatterns = *），生产环境应限制为具体域名</li>
     *     <li>允许的 HTTP 方法：GET、POST、PUT、DELETE、OPTIONS</li>
     *     <li>允许所有请求头</li>
     *     <li>允许携带凭证（Cookie、Authorization 头等）</li>
     *     <li>预检请求缓存时间：3600 秒（1小时）</li>
     * </ul>
     * 注意：此配置与 SecurityConfig 中的 CORS 配置互补，确保非安全过滤链的请求也能正确处理跨域。
     * </p>
     *
     * @param registry 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 对所有路径生效
                .allowedOriginPatterns("*") // 允许所有来源（生产环境应限制）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true) // 允许携带认证凭证
                .maxAge(3600); // 预检请求缓存时间（秒）
    }
}
