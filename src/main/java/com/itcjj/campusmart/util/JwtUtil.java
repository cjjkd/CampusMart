package com.itcjj.campusmart.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${campusmart.jwt.secret}")
    private String secret;

    @Value("${campusmart.jwt.expire}")
    private long expire;

    // 生成 token：传入 userId，返回一串 JWT
    public String createToken(Long userId, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))                                // 放 userId
                .claim("role", role)
                .issuedAt(new Date())                                           // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expire))      // 过期时间
                .signWith(getKey())                                             // 用密钥签名
                .compact();                                                     // 打包成字符串
    }

    // 解析 token；token 有问题会抛异常
    public Claims parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())          // 用密钥验签
                .build()
                .parseSignedClaims(token)      // 解析（签名不对 / 过期都会抛异常）
                .getPayload();                 // 取出 Payload
        return claims;
    }

    // 把配置里的字符串密钥，转成签名要用的 SecretKey 对象
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }
}
