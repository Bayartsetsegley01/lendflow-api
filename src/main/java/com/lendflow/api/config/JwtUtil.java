// src/main/java/com/lendflow/api/config/JwtUtil.java
package com.lendflow.api.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

// JWT token үүсгэх, задлах, шалгах логикийг нэг дор агуулна
@Component
public class JwtUtil {

    private final SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
    private final long expirationMs = 86_400_000; // 24 цаг

    // Хэрэглэгчийн имэйл болон id-г шифрлэж, token үүсгэнэ
    public String generateToken(Long userId, String email) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    // Token-оос имэйлийг задлан гаргаж авна
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Token хугацаа дуусаагүй, зөв гарын үсэгтэй эсэхийг шалгана
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}