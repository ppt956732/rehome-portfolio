package com.rehome.main.utils;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class MemJwtUtil {
    
    // 產生 HMAC-SHA256 簽章的金鑰
    @Value("${jwt.secret}")
    private String secret;

    // 有效期毫秒數
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // 7~30 天（毫秒）

    /* ================= Key ================= */
    // 生成金鑰
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /* ================= Access Token ================= */
    // 生成 Token
    public String generateAccessToken(String email, Integer memberId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))   // 唯一識別
                .claim("email", email)
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // 驗證 Token
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return "access".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }
    
    /* ================= Token 解析 ================= */
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getMemberIdFromAccessToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String getEmailFromAccessToken(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public String getRoleFromAccessToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /* ================= Refresh Token ================= */
    /**
     * 產生 Refresh Token（純隨機字串）
     */
    public String generateRefreshToken() {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Refresh Token 過期時間
     */
    public Date getRefreshTokenExpiryDate() {
        return new Date(System.currentTimeMillis() + refreshTokenExpiration);
    }
}
