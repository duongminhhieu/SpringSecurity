package com.learning.springsecurity.common.configs.security;

import com.learning.springsecurity.token.InvalidTokenRepository;
import com.learning.springsecurity.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${application.jwt.secret-key}")
    private String secretKey;

    @Value("${application.jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${application.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private final InvalidTokenRepository invalidTokenRepository;

    public String extractUserEmail(String jwtToken) {
        return extractClaim(jwtToken, Claims::getSubject);
    }

    public String extractTokenType(String jwtToken) {
        return extractClaim(jwtToken, claims -> claims.get("type", String.class));
    }

    public List<String> extractScope(String jwtToken) {
        return Arrays.asList(extractClaim(jwtToken, claims -> claims.get("scope", String.class)).split(" "));
    }

    public String extractIdToken(String token) {
        return extractClaim(token, Claims::getId);
    }

    public boolean isTokenValid(String jwtToken) {
        String idToken = extractIdToken(jwtToken);
        return !invalidTokenRepository.existsByIdToken(idToken) && !isTokenExpired(jwtToken);
    }


    private boolean isTokenExpired(String jwtToken) {
        return extractExpiration(jwtToken).before(new Date());
    }

    public Date extractExpiration(String jwtToken) {
        return extractClaim(jwtToken, Claims::getExpiration);
    }


    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        claims.put("scope", buildScope(user));
        return generateToken(claims, user, accessTokenExpiration);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return generateToken(claims, user, refreshTokenExpiration);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            User user,
            long expirationTime
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(Instant.now().plus(expirationTime, ChronoUnit.SECONDS).toEpochMilli()))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .setId(UUID.randomUUID().toString())
                .compact();
    }


    public <T> T extractClaim(String jwtToken, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jwtToken);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String jwtToken) {

        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String buildScope(User user) {
        StringJoiner joiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                joiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions()
                            .forEach(permission -> joiner.add(permission.getName()));

            });
        }
        return joiner.toString(); // "ROLE_USER READ WRITE"
    }
}