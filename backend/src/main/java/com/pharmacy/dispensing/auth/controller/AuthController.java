package com.pharmacy.dispensing.auth.controller;

import com.pharmacy.dispensing.auth.dto.*;
import com.pharmacy.dispensing.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        AuthResponse response = authService.login(loginRequest, ipAddress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest refreshRequest, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        AuthResponse response = authService.refreshToken(refreshRequest, ipAddress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshTokenRequest logoutRequest,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       HttpServletRequest request) {
        String username = userDetails != null ? userDetails.getUsername() : "ANONYMOUS";
        String refreshTokenStr = logoutRequest != null ? logoutRequest.getRefreshToken() : null;
        authService.logout(refreshTokenStr, username, getClientIp(request));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal UserDetails userDetails) {
        UserDto userDto = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(userDto);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
