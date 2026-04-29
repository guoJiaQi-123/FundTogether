package com.fundtogether.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fundtogether.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

/**
 * Spring Security 安全配置类
 * <p>
 * 负责配置应用的整体安全策略，包括：
 * <ul>
 *     <li>HTTP 请求的认证与授权规则（哪些路径需要登录、哪些需要特定角色）</li>
 *     <li>CORS 跨域策略</li>
 *     <li>CSRF 防护策略</li>
 *     <li>JWT 认证过滤器的注册</li>
 *     <li>密码编码器的配置</li>
 * </ul>
 * 当前采用前后端分离架构，禁用了 CSRF，使用 JWT Token 进行无状态认证。
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** JWT 认证过滤器，用于从请求头中提取并验证 JWT Token，完成用户身份认证 */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置 Spring Security 过滤器链
     * <p>
     * 定义 HTTP 安全策略的核心方法，配置内容包括：
     * <ol>
     *     <li>禁用 CSRF —— 前后端分离架构下使用 JWT 认证，不需要 CSRF 保护</li>
     *     <li>启用 CORS —— 使用下方 {@link #corsConfigurationSource()} 定义的跨域配置</li>
     *     <li>配置路径授权规则：
     *         <ul>
     *             <li>OPTIONS 请求全部放行（预检请求）</li>
     *             <li>注册、登录、重置密码、支付宝回调等接口放行</li>
     *             <li>公开接口（项目推荐、奖励、公告、评论、关注、收藏等）放行</li>
     *             <li>静态资源路径（/uploads/）放行</li>
     *             <li>管理员接口需要 ADMIN 角色</li>
     *             <li>赞助商接口需要 SPONSOR 或 ADMIN 角色</li>
     *             <li>其余所有请求均需认证</li>
     *         </ul>
     *     </li>
     *     <li>在 UsernamePasswordAuthenticationFilter 之前插入 JWT 过滤器</li>
     * </ol>
     * </p>
     *
     * @param http HttpSecurity 构建器
     * @return 构建完成的 SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // 禁用CSRF防护（JWT无状态认证不需要）
            .cors(Customizer.withDefaults()) // 启用CORS，使用默认的corsConfigurationSource Bean
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll() // 放行所有OPTIONS预检请求
                .requestMatchers("/api/user/register", "/api/user/login", "/api/user/reset-password", "/api/user/account/alipay/notify", "/api/user/account/alipay/return").permitAll() // 放行注册、登录、重置密码、支付宝回调接口
                .requestMatchers("/api/public/**", "/api/comment/project/**", "/api/projects/recommend", "/api/projects/*/rewards", "/api/notices/active", "/api/follow/status/**", "/api/follow/following/**", "/api/follow/followers/**").permitAll() // 放行公开业务接口
                .requestMatchers("/api/favorite/status/**").permitAll() // 放行收藏状态查询接口
                .requestMatchers("/uploads/**").permitAll() // 放行静态资源（上传文件）访问路径
                .requestMatchers("/api/funding/process-payout/**").hasRole("ADMIN") // 众筹打款处理接口仅管理员可访问
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // 管理员后台接口仅ADMIN角色可访问
                .requestMatchers("/api/sponsor/**").hasAnyRole("SPONSOR", "ADMIN") // 赞助商接口需SPONSOR或ADMIN角色
                .anyRequest().authenticated() // 其余所有请求均需认证
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // 在用户名密码认证过滤器之前插入JWT过滤器

        return http.build();
    }

    /**
     * 配置 CORS 跨域策略
     * <p>
     * 定义跨域资源共享（CORS）的详细规则：
     * <ul>
     *     <li>允许所有来源（allowedOriginPatterns = *），生产环境应限制为具体域名</li>
     *     <li>允许的 HTTP 方法：GET、POST、PUT、DELETE、OPTIONS</li>
     *     <li>允许所有请求头</li>
     *     <li>允许携带凭证（Cookie、Authorization 头等）</li>
     *     <li>跨域配置应用于所有路径（/**）</li>
     * </ul>
     * </p>
     *
     * @return CORS 配置源，供 Spring Security 的 CORS 模块引用
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // 允许所有来源（生产环境应限制）
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 允许的HTTP方法
        configuration.setAllowedHeaders(Arrays.asList("*")); // 允许所有请求头
        configuration.setAllowCredentials(true); // 允许携带认证凭证
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 对所有路径生效
        return source;
    }

    /**
     * 配置密码编码器
     * <p>
     * 使用 BCrypt 算法对用户密码进行哈希加密，BCrypt 是一种自适应哈希算法，
     * 内部自动加盐，安全性高于 MD5/SHA 等简单哈希算法。
     * 用于用户注册时加密密码和登录时校验密码。
     * </p>
     *
     * @return BCrypt 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
