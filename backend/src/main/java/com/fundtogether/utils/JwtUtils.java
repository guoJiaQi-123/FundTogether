package com.fundtogether.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * <p>
 * 提供JWT令牌的生成和解析功能，用于实现无状态的用户认证机制。
 * 使用HMAC-SHA256算法对令牌进行签名，确保令牌的完整性和不可篡改性。
 * 令牌中携带用户ID、账号和角色信息，并设置24小时的有效期。
 * </p>
 */
public class JwtUtils {

    /** HMAC-SHA256签名密钥，由Keys.secretKeyFor自动生成安全随机密钥 */
    private static final Key KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    /** 令牌过期时间：24小时（单位：毫秒） */
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;

    /**
     * 生成JWT令牌
     * <p>
     * 将用户ID、账号和角色信息写入令牌的声明（Claims）中，
     * 并设置过期时间为当前时间后的24小时，最后使用密钥进行签名。
     * </p>
     *
     * @param userId  用户ID，唯一标识用户
     * @param account 用户账号名
     * @param role    用户角色编码
     * @return 签名后的JWT令牌字符串
     */
    public static String generateToken(Long userId, String account, Integer role) {
        // 构建自定义声明信息
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("account", account);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims) // 设置自定义声明
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME)) // 设置过期时间
                .signWith(KEY) // 使用密钥签名
                .compact(); // 构建并压缩为令牌字符串
    }

    /**
     * 解析JWT令牌
     * <p>
     * 验证令牌的签名和有效期，若验证通过则返回令牌中的声明信息。
     * 若令牌无效（签名不匹配、已过期等），将抛出相应的异常。
     * </p>
     *
     * @param token 待解析的JWT令牌字符串
     * @return 令牌中的声明信息（Claims），包含userId、account、role等字段
     * @throws io.jsonwebtoken.JwtException 令牌无效时抛出异常
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY) // 设置验签密钥
                .build()
                .parseClaimsJws(token) // 解析并验证令牌
                .getBody(); // 返回声明主体
    }
}
