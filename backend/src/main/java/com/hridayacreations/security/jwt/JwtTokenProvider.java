package com.hridayacreations.security.jwt;

import com.hridayacreations.config.AppProperties;
import com.hridayacreations.security.services.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

/**
 * Issues and validates stateless JWT access tokens (HMAC-SHA signed). The subject is the user id;
 * email and roles are carried as custom claims for convenience.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final String issuer;

    public JwtTokenProvider(AppProperties properties) {
        AppProperties.Jwt jwt = properties.getJwt();
        this.signingKey = buildSigningKey(jwt.getSecret());
        this.accessTokenExpirationMs = jwt.getAccessTokenExpirationMs();
        this.issuer = jwt.getIssuer();
    }

    /**
     * Derives a 256-bit HMAC signing key from the configured secret. A valid Base64 secret that
     * already yields ≥32 bytes is used directly; any other value (e.g. a platform-generated secret)
     * is hashed with SHA-256 so the application always has a valid HS256 key.
     */
    private static SecretKey buildSigningKey(String secret) {
        byte[] keyBytes;
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            keyBytes = decoded.length >= 32 ? decoded : sha256(secret);
        } catch (Exception ex) {
            keyBytes = sha256(secret);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }

    /**
     * Generates a signed access token for the authenticated principal.
     */
    public String generateAccessToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(String.valueOf(principal.getId()))
                .claim(CLAIM_EMAIL, principal.getEmail())
                .claim(CLAIM_ROLES, roles)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).get(CLAIM_EMAIL, String.class);
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    /**
     * Validates signature and expiry of a token.
     *
     * @return {@code true} when the token is well-formed, correctly signed and not expired
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.debug("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.debug("Unsupported JWT token: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.debug("Malformed JWT token: {}", ex.getMessage());
        } catch (SignatureException ex) {
            log.debug("Invalid JWT signature: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.debug("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
