package com.pharmacy.dispensing.auth.service;

import com.pharmacy.dispensing.audit.service.AuditEventService;
import com.pharmacy.dispensing.auth.dto.*;
import com.pharmacy.dispensing.auth.entity.RefreshToken;
import com.pharmacy.dispensing.auth.entity.Role;
import com.pharmacy.dispensing.auth.entity.User;
import com.pharmacy.dispensing.auth.repository.RefreshTokenRepository;
import com.pharmacy.dispensing.auth.repository.UserRepository;
import com.pharmacy.dispensing.common.exception.InvalidCredentialsException;
import com.pharmacy.dispensing.common.exception.ResourceNotFoundException;
import com.pharmacy.dispensing.common.exception.TokenExpiredException;
import com.pharmacy.dispensing.common.security.JwtTokenProvider;
import com.pharmacy.dispensing.common.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final AuditEventService auditEventService;

    @Value("${jwt.access-token-expiration-ms:900000}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs; // 7 days

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtTokenProvider tokenProvider,
                       AuditEventService auditEventService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProvider = tokenProvider;
        this.auditEventService = auditEventService;
    }

    @Transactional
    public AuthResponse login(LoginRequest loginRequest, String ipAddress) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (Exception ex) {
            auditEventService.logEvent("LOGIN_FAILED", loginRequest.getUsername(), "Failed login attempt", null, ipAddress);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken = tokenProvider.generateAccessToken(authentication);
        RefreshToken refreshToken = createRefreshToken(user);

        UserDto userDto = mapToUserDto(user);

        auditEventService.logEvent("USER_LOGIN", user.getUsername(), "User logged in successfully", null, ipAddress);

        return new AuthResponse(accessToken, refreshToken.getToken(), accessTokenExpirationMs, userDto);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        if (Boolean.TRUE.equals(token.getRevoked())) {
            throw new InvalidCredentialsException("Refresh token has been revoked");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenExpiredException("Refresh token has expired. Please login again.");
        }

        User user = token.getUser();
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        UserDto userDto = mapToUserDto(user);

        return new AuthResponse(newAccessToken, token.getToken(), accessTokenExpirationMs, userDto);
    }

    @Transactional
    public void logout(String refreshTokenStr, String username, String ipAddress) {
        if (refreshTokenStr != null) {
            refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
        auditEventService.logEvent("USER_LOGOUT", username, "User logged out successfully", null, ipAddress);
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToUserDto(user);
    }

    private RefreshToken createRefreshToken(User user) {
        // Delete previous tokens for clean state
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpirationMs));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    private UserDto mapToUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getActive(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );
    }
}
