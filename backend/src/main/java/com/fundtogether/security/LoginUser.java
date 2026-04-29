package com.fundtogether.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 登录用户信息封装类
 * <p>
 * 用于在Spring Security认证流程中承载当前登录用户的核心信息。
 * 该对象会被设置为UsernamePasswordAuthenticationToken的Principal（主体），
 * 后续可通过SecurityContextHolder获取当前登录用户的详细信息。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
    /** 用户ID，唯一标识一个用户 */
    private Long id;
    /** 用户账号名，用于登录 */
    private String account;
    /** 用户角色编码，对应UserRole枚举的值 */
    private Integer role;
    /** 用户拥有的权限集合，由Spring Security的GrantedAuthority实现类组成 */
    private Collection<? extends GrantedAuthority> authorities;
}
