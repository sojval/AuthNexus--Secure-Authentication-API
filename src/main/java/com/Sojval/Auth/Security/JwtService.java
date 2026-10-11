package com.Sojval.Auth.Security;

import com.Sojval.Auth.Entity.JwtProperties;
import com.Sojval.Auth.Entity.Role;
import com.Sojval.Auth.Entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Getter
@Setter
public class JwtService {
    private final JwtProperties jwtProperties;
    private SecretKey key;
    @PostConstruct
    public void init() {
        // Option A: Convert a plain text / base64 secret string into a SecretKey
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }


    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles() == null ? List.of() :
                user.getRoles().stream().map(Role::getName).toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.getExpirationMs())))
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("type", "access")
                .signWith(key)
                .compact();
    }
    public String generateRefreshToken(User user, String jti) {
        Instant now = Instant.now();

        return Jwts.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(
                        now.plusMillis(
                                jwtProperties.getRefreshExpirationMs()
                        )
                ))
                .claim("type", "refresh")
                .signWith(key)
                .compact();
    }
    public Jws<Claims> parseToken(String token) {
        try{
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
        }
        catch (Exception e){
            throw new JwtException("Invalid token");
        }
    }
    public boolean isAccessTokenValid(String token) {
        Claims c = parseToken(token).getPayload();
        return "access".equals(c.get("type"));
    }
    public boolean isRefreshToken(String token) {
        Claims c = parseToken(token).getPayload();
        return "refresh".equals(c.get("type"));
    }
    public Long getUserId(String token) {
        Claims c = parseToken(token).getPayload();
        return Long.valueOf(c.getSubject());
    }
    public String getJti(String token) {
        return parseToken(token).getPayload().getId();
    }
    public List<String> getRoles(String token) {
        Claims c = parseToken(token).getPayload();
        return (List<String>) c.get("roles");
    }
    public String getEmail(String token) {
        Claims c = parseToken(token).getPayload();
        return (String) c.get("email");
    }
}
