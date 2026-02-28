package com.ecommers.auth_service.controller;

import com.ecommers.auth_service.dto.*;
import com.ecommers.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ================= REGISTER =================

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody RegisterRequest request) {

        String response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    // ================= VERIFY OTP =================

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @RequestBody VerifyOtpRequest request) {

        return ResponseEntity.ok(
                authService.verifyOtp(
                        request.getEmail(),
                        request.getOtp()
                )
        );
    }

    // ================= RESEND OTP =================

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(
            @RequestParam String email) {

        authService.resendOtp(email);
        return ResponseEntity.ok("OTP resent successfully");
    }

    // ================= LOGIN =================

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody RefreshRequest request
    ){
        return ResponseEntity.ok(
                authService.refreshToken(request.getRefreshToken())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestParam String refreshToken
    ){
        authService.logout(refreshToken);
        return ResponseEntity.ok("Logged Out Successfully");
    }

}
