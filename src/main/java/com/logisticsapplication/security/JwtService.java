package com.logisticsapplication.security;

import com.logisticsapplication.dto.response.AppUserResponse;
import com.logisticsapplication.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(AppUserResponse user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.expirationSeconds());
        return Jwts.builder()
                .subject(user.getLogin())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractLogin(String token) {
        return extractClaims(token).getSubject();
    }

    public UserRole extractRole(String token) {
        String role = extractClaims(token).get("role", String.class);
        return UserRole.valueOf(role);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
