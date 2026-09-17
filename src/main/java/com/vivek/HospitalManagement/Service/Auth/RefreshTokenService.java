package com.vivek.HospitalManagement.Service.Auth;

import com.vivek.HospitalManagement.Entity.RefreshToken;
import com.vivek.HospitalManagement.Entity.User;
import com.vivek.HospitalManagement.Exceptions.BadRequestException;
import com.vivek.HospitalManagement.Exceptions.ResourceNotFoundException;
import com.vivek.HospitalManagement.Repository.RefreshTokenRepository;
import com.vivek.HospitalManagement.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository) {

        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setToken(
                UUID.randomUUID().toString()
        );

        refreshToken.setCreatedAt(
                LocalDateTime.now()
        );

        refreshToken.setExpiresAt(
                LocalDateTime.now().plusDays(7)
        );

        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid refresh token"
                                ));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException(
                    "Refresh token has been revoked"
            );
        }

        if (refreshToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Refresh token has expired"
            );
        }

        return refreshToken;
    }

    @Transactional
    public void revokeToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Refresh token not found"
                                ));

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken rotateToken(String token) {

        RefreshToken oldToken =
                refreshTokenRepository
                        .findByToken(token)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Invalid refresh token"
                                ));

        if (oldToken.isRevoked()) {
            throw new BadRequestException(
                    "Refresh token has already been used"
            );
        }

        if (oldToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "Refresh token has expired"
            );
        }

        oldToken.setRevoked(true);

        refreshTokenRepository.save(oldToken);

        return createRefreshToken(oldToken.getUser());
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {

        refreshTokenRepository.revokeAllByUserId(userId);
    }
}