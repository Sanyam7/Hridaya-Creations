package com.hridayacreations.service.impl;

import com.hridayacreations.config.AppProperties;
import com.hridayacreations.entity.RefreshToken;
import com.hridayacreations.entity.User;
import com.hridayacreations.exception.TokenRefreshException;
import com.hridayacreations.repository.RefreshTokenRepository;
import com.hridayacreations.service.interfaces.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Refresh token management backed by the database, enabling stateful revocation on top of stateless
 * access tokens.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public RefreshToken create(User user) {
        Instant now = Instant.now();
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(now.plusMillis(appProperties.getJwt().getRefreshTokenExpirationMs()))
                .revoked(false)
                .createdAt(now)
                .build();
        return refreshTokenRepository.save(token);
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken verifyUsable(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));
        if (refreshToken.isExpired()) {
            throw new TokenRefreshException("Refresh token has expired. Please sign in again.");
        }
        if (refreshToken.isRevoked()) {
            throw new TokenRefreshException("Refresh token has been revoked. Please sign in again.");
        }
        return refreshToken;
    }

    @Override
    @Transactional
    public RefreshToken rotate(String token) {
        RefreshToken current = verifyUsable(token);
        current.setRevoked(true);
        refreshTokenRepository.save(current);
        return create(current.getUser());
    }

    @Override
    @Transactional
    public void revoke(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Override
    @Transactional
    public void revokeAllForUser(User user) {
        refreshTokenRepository.revokeAllByUser(user);
    }
}
