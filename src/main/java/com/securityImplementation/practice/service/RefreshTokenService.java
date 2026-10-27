package com.securityImplementation.practice.service;

import com.securityImplementation.practice.dtos.LoginResponse;
import com.securityImplementation.practice.entity.AppUser;
import com.securityImplementation.practice.entity.RefreshToken;
import com.securityImplementation.practice.exception.InvalidTokenException;
import com.securityImplementation.practice.repository.AppUserRepo;
import com.securityImplementation.practice.repository.RefreshTokenRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final long expiryDays;
    private final AppUserRepo userRepo;
    private final RefreshTokenRepo refreshTokenRepo;
    private final JwtService jwtService;

    public RefreshTokenService(@Value("${jwt.expiration.in-days}") long expiryDays,
                               AppUserRepo userRepo,
                               RefreshTokenRepo refreshTokenRepo,
                               JwtService jwtService) {
        this.expiryDays = expiryDays;
        this.userRepo = userRepo;
        this.refreshTokenRepo = refreshTokenRepo;
        this.jwtService = jwtService;
    }

    /** Saves the HASH in the DB and returns the RAW token for the client. */
    public String createRefreshToken(String username) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        String raw = UUID.randomUUID().toString();

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(sha256(raw));
        token.setExpiry(LocalDateTime.now().plusDays(expiryDays));
        refreshTokenRepo.save(token);

        return raw;
    }

    // noRollbackFor: the "revoke all on reuse" update must be kept even though we then throw
    @Transactional(noRollbackFor = InvalidTokenException.class)
    public LoginResponse refresh(String rawToken) {
        String hash = sha256(rawToken);

        RefreshToken old = refreshTokenRepo.findByToken(hash)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (old.isRevoked()) {                                   // reuse detected
            refreshTokenRepo.revokeAllForUser(old.getUser().getId());
            throw new InvalidTokenException("Refresh token reuse detected. Please log in again.");
        }
        if (old.getExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired");
        }
        if (refreshTokenRepo.revokeIfActive(hash) == 0) {        // lost a race with another request
            throw new InvalidTokenException("Refresh token already used");
        }

        String username = old.getUser().getUsername();
        return new LoginResponse(jwtService.generateToken(username), createRefreshToken(username));
    }

    @Transactional
    public void logout(String rawToken) {
        refreshTokenRepo.revokeIfActive(sha256(rawToken));
    }

    private static String sha256(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}