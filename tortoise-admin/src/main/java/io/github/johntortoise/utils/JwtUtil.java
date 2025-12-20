package io.github.johntortoise.utils;

import io.github.johntortoise.core.utils.LogUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;


@Slf4j
public class JwtUtil {
    private static final String SECRET_STRING = "tortoise-system-secret-key-for-jwt-token-generation-2025";
    private static final Key SECRET_KEY = new SecretKeySpec(SECRET_STRING.getBytes(), SignatureAlgorithm.HS256.getJcaName());
    
    private static final long EXPIRATION_TIME = 86400000;


    public static String generateToken(Long userId, String email, Long roleCode) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("用户邮箱不能为空");
        }
        if (roleCode == null) {
            throw new IllegalArgumentException("角色代码不能为空");
        }

        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

            return Jwts.builder()
                    .setSubject(email)
                    .claim("userId", userId)
                    .claim("roleCode", roleCode)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(SECRET_KEY)
                    .compact();
        } catch (Exception e) {
            LogUtil.error("生成JWT Token失败", e);
            throw new RuntimeException("生成Token失败", e);
        }
    }


    public static Claims parseToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            LogUtil.warn("Token已过期: {}", token);
            throw e;
        } catch (MalformedJwtException e) {
            LogUtil.warn("Token格式错误: {}", token);
            throw e;
        } catch (SignatureException e) {
            LogUtil.warn("Token签名验证失败: {}", token);
            throw e;
        } catch (JwtException e) {
            LogUtil.error("解析Token失败", e);
            throw e;
        }
    }

    
    public static Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            LogUtil.warn("从Token获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    
    public static String getEmailFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            LogUtil.warn("从Token获取邮箱失败: {}", e.getMessage());
            return null;
        }
    }

    
    public static Long getRoleCodeFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("roleCode", Long.class);
        } catch (Exception e) {
            LogUtil.warn("从Token获取角色代码失败: {}", e.getMessage());
            return null;
        }
    }

    
    public static boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration() != null && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            LogUtil.debug("Token验证失败: {}", e.getMessage());
            return false;
        }
    }
}