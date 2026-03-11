package com.ecommers.auth_service.controller;

import com.ecommers.auth_service.dto.*;
import com.ecommers.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<Void> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response) {

       authService.login(request,response);
        return ResponseEntity.ok().build();
    }

    // ================= REFRESH =================

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody RefreshRequest request
    ){
        return ResponseEntity.ok(
                authService.refreshToken(request.getRefreshToken())
        );
    }

    // ================= LOGOUT =================

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ){
        authService.logout(request,response);
        return ResponseEntity.ok().build();
    }

    // ================= PROFILE =================

    @GetMapping("/profile")
    public ResponseEntity<AuthResponse> profile(){
        AuthResponse response = authService.getProfile();
        return ResponseEntity.ok(response);
    }


    // ================= FORGOT-PASSWORD =================


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody String email){
        return ResponseEntity.ok(authService.forgotPassword(email));
    }

    // ================= VERIFY-RESET-OTP =================

    @PostMapping("/verify-rest-otp")
    public ResponseEntity<String> verifyResetOtp(
            @RequestParam String email,
            @RequestParam String otp
    ){
        return ResponseEntity.ok(
                authService.verifyResendOtp(email,otp)
        );
    }

    // ================= RESET-PASSWORD =================

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        return ResponseEntity.ok(authService.resetPassword(request));
    }


}
