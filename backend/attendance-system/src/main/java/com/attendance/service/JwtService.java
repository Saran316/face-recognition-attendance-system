
package com.attendance.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(now))
                .expiration(new Date(now + 1000L * 60 * 60))
                .signWith(secretKey)
                .compact();
    }
    
public String extractEmail(String token) {
    return io.jsonwebtoken.Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
}

public boolean isTokenValid(String token, String email) {
    try {
        return email.equals(extractEmail(token));
    } catch (io.jsonwebtoken.JwtException |
             IllegalArgumentException exception) {
        return false;
    }
}

}
