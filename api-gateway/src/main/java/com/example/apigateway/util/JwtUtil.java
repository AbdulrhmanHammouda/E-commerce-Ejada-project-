package com.example.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // Validate the token signature and expiration
    public void validateToken(final String token) {
        Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token);
    }

    // Extract the role from the token payload
    public String extractRole(final String token) {
        Claims claims = Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
        return claims.get("role", String.class);
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
