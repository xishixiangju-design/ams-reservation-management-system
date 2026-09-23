package com.trae.ams.common.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * JWT令牌工具类
 * 
 * 功能说明：
 * 提供JWT令牌的创建、验证和解析功能，用于用户认证和授权。
 * 
 * 令牌结构：
 * - iat: 签发时间
 * - exp: 过期时间
 * - userId: 用户ID
 * - username: 用户名
 * 
 * 配置项：
 * - jwt.secret: JWT签名密钥
 * - jwt.expiration: 令牌有效期（毫秒）
 */
@Component
public class JwtUtil {

    /** JWT签名密钥，从配置文件读取 */
    @Value("${jwt.secret}")
    private String secret;

    /** JWT令牌有效期（毫秒），从配置文件读取 */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 创建JWT令牌
     * 
     * @param userId 用户ID，存入令牌载荷中
     * @param username 用户名，存入令牌载荷中
     * @return 生成的JWT令牌字符串
     */
    public String createToken(Long userId, String username) {
        Map<String, Object> payload = new HashMap<>();
        long now = System.currentTimeMillis();
        payload.put(JWTPayload.ISSUED_AT, now); // 签发时间
        payload.put(JWTPayload.EXPIRES_AT, now + expiration); // 过期时间
        payload.put("userId", userId); // 用户ID
        payload.put("username", username); // 用户名
        
        // 使用密钥对载荷进行签名，生成JWT令牌
        return JWTUtil.createToken(payload, secret.getBytes());
    }

    /**
     * 验证JWT令牌是否有效
     * 验证流程：1.验证签名是否正确 2.验证令牌是否过期
     * 
     * @param token 待验证的JWT令牌
     * @return 令牌有效返回true，签名错误或已过期返回false
     */
    public boolean verify(String token) {
        try {
            // 步骤1：验证签名是否正确
            boolean verified = JWTUtil.verify(token, secret.getBytes());
            if (!verified) {
                return false;
            }
            // 步骤2：验证令牌是否过期
            JWT jwt = JWTUtil.parseToken(token);
            long now = System.currentTimeMillis();
            Object exp = jwt.getPayload(JWTPayload.EXPIRES_AT);
            if (exp == null) {
                return false; // 没有过期时间的令牌视为无效
            }
            long expTime = Long.parseLong(exp.toString());
            return now < expTime; // 当前时间小于过期时间则令牌有效
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析JWT令牌
     * 
     * @param token JWT令牌字符串
     * @return 解析后的JWT对象
     */
    public JWT parse(String token) {
        return JWTUtil.parseToken(token);
    }
    
    /**
     * 从JWT令牌中获取用户ID
     * 
     * @param token JWT令牌字符串
     * @return 用户ID，令牌无效时返回null
     */
    public Long getUserId(String token) {
        Object userId = parse(token).getPayload("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }
}
