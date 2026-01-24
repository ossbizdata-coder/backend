package com.oss.security;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Component
public class JwtUtil {
    private final String SECRET = "your256bitlongsecretkeyyour256bitlongsecretkey"; // load from env in prod
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }
    // New: include role in token
    public String generateToken(UserDetails userDetails, String role, Long id) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("id", id);
        return createToken(claims, userDetails.getUsername());
    }
    private String createToken(Map<String, Object> claims, String subject) {
        // Set to 10 years (effectively unlimited until server restart)
        // Since PIN verification is used, token doesn't need to expire
        long expirationTime = 10L * 365 * 24 * 60 * 60 * 1000; // 10 years in milliseconds
        return Jwts.builder()
                   .setClaims(claims)
                   .setSubject(subject)
                   .setIssuedAt(new Date(System.currentTimeMillis()))
                   .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                   .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                   .compact();
    }
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getSigningKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .getSubject();
    }
    public String extractRole(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getSigningKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .get("role", String.class);
    }
    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }
    public boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getSigningKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .getExpiration()
                   .before(new Date());
    }
}