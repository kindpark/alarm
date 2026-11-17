package com.msa.alarm.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKeyString;

    private SecretKey secretKey;
    // 32자 검증
    @PostConstruct
    public void init() {
        // 빈 문자 및 null 체크
        if (secretKeyString == null || secretKeyString.isBlank()){
            throw new IllegalArgumentException("JWT 비밀키가 설정되지 않음.");
        }
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32){
            throw new IllegalArgumentException("JWT 비밀키는 최소 32바이트 이상이어야 합니다.");
        }
        // 싱글톤 SecretKey 객체 생성
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 토큰 유효성 및 만료 여부 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    // 토큰에서 사용자 ID 추출
    public Long getUserIdFromToken(String token){
        String subject = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.valueOf(subject);
    }
    // 토큰에서 역할 추출
    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}
