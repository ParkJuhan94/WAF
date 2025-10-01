package dev.waf.console.infrastructure.security;

import java.util.Date;

import javax.crypto.SecretKey;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long tokenValidityInMilliseconds;

    public JwtTokenProvider(@Value("${app.jwt.secret:defaultSecretKeyForDevelopmentUseOnly123456789}") String secretKey,
                           @Value("${app.jwt.token-validity-in-seconds:86400}") long tokenValidityInSeconds) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }

    public String createToken(String userId, String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public String getUserId(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}