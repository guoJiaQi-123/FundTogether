package com.fundtogether.security;

import com.fundtogether.common.enums.UserRole;
import com.fundtogether.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证过滤器
 * <p>
 * 继承自Spring Security的OncePerRequestFilter，确保每个HTTP请求只经过一次该过滤器。
 * 主要职责：从HTTP请求头中提取JWT令牌，解析并验证令牌的合法性，
 * 然后将解析出的用户信息封装为认证对象，存入Spring Security的安全上下文中，
 * 从而实现基于JWT的无状态认证机制。
 * </p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * 对每个HTTP请求进行JWT认证过滤处理
     * <p>
     * 流程如下：
     * 1. 从请求头中提取Bearer Token；
     * 2. 若Token存在，则解析Token获取用户信息（用户ID、账号、角色）；
     * 3. 根据用户角色构建对应的权限列表；
     * 4. 将用户信息和权限封装为LoginUser对象，并存入SecurityContext；
     * 5. 若Token解析失败，则忽略异常（不设置认证信息，由后续安全机制拦截）；
     * 6. 无论Token是否有效，都继续执行后续过滤器链。
     * </p>
     *
     * @param request     当前HTTP请求对象
     * @param response    当前HTTP响应对象
     * @param filterChain  过滤器链，用于调用下一个过滤器
     * @throws ServletException Servlet异常
     * @throws IOException      IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 从请求头中提取JWT令牌
        String token = getJwtFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                // 解析JWT令牌，获取声明信息
                Claims claims = JwtUtils.parseToken(token);
                // 从令牌中提取用户ID
                Long userId = claims.get("userId", Long.class);
                // 从令牌中提取用户账号
                String account = claims.get("account", String.class);
                // 从令牌中提取用户角色编码
                Integer role = claims.get("role", Integer.class);

                // 构建用户权限列表，所有已认证用户默认拥有ROLE_USER权限
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                // 若用户为发起人角色，追加ROLE_SPONSOR权限
                if (UserRole.isSponsor(role)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_SPONSOR"));
                }
                // 若用户为管理员角色，追加ROLE_ADMIN权限
                if (UserRole.isAdmin(role)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }

                // 将用户信息封装为LoginUser对象
                LoginUser loginUser = new LoginUser(userId, account, role, authorities);

                // 构建Spring Security认证令牌，包含用户主体、凭证（null）和权限列表
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        loginUser, null, authorities);

                // 将认证信息存入SecurityContext，后续可通过SecurityContextHolder获取当前登录用户
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // Token解析失败，不设置认证信息，请求将被Spring Security视为未认证
            }
        }

        // 继续执行后续过滤器和目标Servlet
        filterChain.doFilter(request, response);
    }

    /**
     * 从HTTP请求头中提取JWT令牌
     * <p>
     * 从Authorization请求头中获取Bearer Token格式的令牌字符串。
     * 标准格式为："Authorization: Bearer &lt;token&gt;"，本方法会截取"Bearer "之后的部分作为令牌。
     * </p>
     *
     * @param request HTTP请求对象
     * @return JWT令牌字符串；若请求头中不存在合法的Bearer Token，则返回null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // 判断请求头是否存在且以"Bearer "开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // 截取"Bearer "之后的令牌部分（"Bearer "长度为7）
            return bearerToken.substring(7);
        }
        return null;
    }
}
