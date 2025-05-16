package com.ieolympicstickets.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String base64Secret;
    @Value("${jwt.validityMs}")
    private long validityMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte [] secretBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(base64Secret);
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }
    /**
     * Generate JWT HMAC-SHA256
     ** */
    public String generateToken(String subject, List<String> roles) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setIssuer("ieolympicstickets")
                .setSubject(subject)
                .claim("roles", roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(validityMs)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate Token (signature & expiration)
     * */
    public boolean validateToken(String token) {
        System.out.println(">>> JwtService – secret = [" + base64Secret + "]");
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Extract subject (email) from token
     ** */
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract roles from Token
     * */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
       return extractClaim(token, claims -> (List<String>) claims.get("roles"));
    }

    // Helpers

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
