package com.ecommers.auth_service.service;

import com.ecommers.auth_service.dto.AuthResponse;
import com.ecommers.auth_service.dto.LoginRequest;
import com.ecommers.auth_service.dto.RegisterRequest;
import com.ecommers.auth_service.dto.ResetPasswordRequest;
import com.ecommers.auth_service.entity.AccountStatus;
import com.ecommers.auth_service.entity.RefreshToken;
import com.ecommers.auth_service.entity.Role;
import com.ecommers.auth_service.entity.User;
import com.ecommers.auth_service.exception.*;
import com.ecommers.auth_service.repsoitory.RefreshTokenRepository;
import com.ecommers.auth_service.repsoitory.UserRepository;
import com.ecommers.auth_service.security.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomerUserDetailService customerUserDetailService;
    private final JwtUtil jwtUtil;
    private final OtpUtil otpUtil;
    private final EmailService emailService;
    private final RedisOtpService redisOtpService;
    private final RedisSecurityService redisSecurityService;
    private final RefreshTokenRepository refreshTokenRepository;
    private static final Logger log =
            LoggerFactory.getLogger(AuthService.class);


    // ========================= REGISTER =========================

    public String register(RegisterRequest request) {

        log.info("Register request received for email: {}", request.getEmail());

        Optional<User> existing = userRepository.findByEmail(request.getEmail());

        if (existing.isPresent()) {

            log.warn("User already exists with email: {}", request.getEmail());

            if (existing.get().getAccountStatus() == AccountStatus.ACTIVE) {
                log.warn("Active account already exists for email: {}", request.getEmail());
                throw new UserAlreadyExistException("Account already exists");
            }
            log.info("Account pending verification. Resending OTP to: {}", request.getEmail());
            resendOtp(request.getEmail());
            return "OTP resent to your email";
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // ✅ FIXED
                .role(Role.ROLE_USER)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .build();

        userRepository.save(user);

        log.info("User saved successfully. Sending OTP to: {}", request.getEmail());

        createAndSendOtp(user.getEmail());

        return "OTP sent to your email";
    }

    // ========================= CREATE OTP =========================

    private void createAndSendOtp(String email) {

        log.info("Generating OTP for email: {}", email);

        redisOtpService.checkResendThrottle(email);

        String otp = otpUtil.generateOtp();

        redisOtpService.saveOtp(email,otp);

        emailService.sendOtp(email,otp);

        log.info("OTP sent successfully to email: {}", email);
    }

    // ========================= RESEND OTP =========================

    public void resendOtp(String email) {

        redisOtpService.checkResendThrottle(email);

        String otp = otpUtil.generateOtp();

        redisOtpService.saveOtp(email,otp);

        emailService.sendOtp(email,otp);
    }

    // ========================= VERIFY OTP =========================

    @Transactional
    public String verifyOtp(String email, String otp) {

        log.info("OTP verification request received for email: {}", email);

        String storedOtp =
                redisOtpService.getOtp(email.trim().toLowerCase());


        if (storedOtp == null){
            log.warn("OTP expired for email: {}", email);
            throw new OtpException("OTP expired");
        }

        if (!storedOtp.equals(otp)){
            log.warn("Invalid OTP attempt for email: {}", email);
            throw new OtpException("Invalid OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found during OTP verification: {}", email);
                    return new ResourceNotFoundException("User not found");
                });

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        redisOtpService.deleteOtp(email);

        log.info("Account verified successfully for email: {}", email);

        return "Account verified successfully";
    }

    // ========================= LOGIN =========================

    public void login(LoginRequest request,HttpServletResponse response) {

        log.info("Login attempts for email: {}",request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("User not found for email: {}", request.getEmail());
                    return new ResourceNotFoundException("Invalid Credentials");
                });

        log.info("User found: {}, Status: {}",
                user.getEmail(), user.getAccountStatus());

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            log.warn("Account not activated for email: {}",user.getEmail());
            throw new AccountNotActiveException("Account not active");
        }

        try{

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            log.info("Authentication successful for email: {}",
                    request.getEmail());

            redisSecurityService
                    .clearFailedLogin(request.getEmail());

        } catch (Exception e){
            log.error("Authentication failed for email: {}",
                    request.getEmail(), e);
            redisSecurityService
                    .recordFailedLogin(request.getEmail());
            throw new InvalidCredentialsException(
                    "Invalid email or password");

        }

        UserDetails userDetails =
                customerUserDetailService.loadUserByUsername(user.getEmail());

        log.info("Generating access & refresh tokens for: {}",
                user.getEmail());

        String accessToken =
                jwtUtil.generateAccessToken(userDetails);

        String refreshToken =
                jwtUtil.generateRefreshToken(userDetails);

        log.info("Deleting old refresh tokens for user: {}",
                user.getEmail());

        refreshTokenRepository.deleteByUser(user);

        log.info("Saving new refresh token for user: {}",
                user.getEmail());

        RefreshToken refreshtokenEntity =
                RefreshToken.builder()
                        .token(refreshToken)
                        .user(user)
                        .expiryDate(LocalDateTime.now().plusDays(7))
                        .build();

        refreshTokenRepository.save(refreshtokenEntity);

        log.info("Storing tokens in cookies");

        addCookie(response,"accessToken",accessToken,15*60);
        addCookie(response,"refreshToken",refreshToken,7*24*60*60);

        log.info("Login completed successfully for: {}",
                user.getEmail());



    }

    private void addCookie(HttpServletResponse response,
                           String name,
                           String value,
                           int maxAge) {

        Cookie cookie = new Cookie(name, value);

        cookie.setHttpOnly(true);
        cookie.setSecure(true); // set false for localhost if needed
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);

        response.addCookie(cookie);
    }

    public AuthResponse refreshToken(String refreshToken){

        log.info("Refresh token request received");

        RefreshToken storedToken =
                refreshTokenRepository.findByToken(refreshToken)
                        .orElseThrow(() -> {
                            log.warn("Invalid refresh token");
                            return new InvalidCredentialsException("Invalid refresh token");
                        });

        if(storedToken.getExpiryDate()
                .isBefore(LocalDateTime.now())){

            log.warn("Refresh token expired for user: {}",
                    storedToken.getUser().getEmail());

            refreshTokenRepository.delete(storedToken);

            throw new InvalidCredentialsException("Refresh token expired");
        }

        User user = storedToken.getUser();

        log.info("Rotating refresh token for user: {}", user.getEmail());

        refreshTokenRepository.delete(storedToken);

        UserDetails userDetails =
                customerUserDetailService.loadUserByUsername(user.getEmail());

        String newAccessToken =
                jwtUtil.generateAccessToken(userDetails);

        String newRefreshToken =
                jwtUtil.generateRefreshToken(userDetails);

        RefreshToken newToken =
                RefreshToken.builder()
                        .token(newRefreshToken)
                        .user(user)
                        .expiryDate(LocalDateTime.now().plusDays(7))
                        .build();
        refreshTokenRepository.save(newToken);

        log.info("Refresh token rotation successful for user: {}", user.getEmail());

        return new AuthResponse(
                "Token Refreshed",
                user.getEmail(),
                user.getRole().name()
        );

    }

    public void logout(HttpServletRequest request,HttpServletResponse response){
        log.info("Logout request received");

        String refreshToken = null;

        if(request.getCookies()!= null){
            for (Cookie cookie: request.getCookies()){
                if(cookie.getName().equals("refreshToken")){
                    refreshToken= cookie.getValue();
                }
            }
        }

        if(refreshToken!=null){
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(refreshTokenRepository::delete);
        }

        clearCookies(response,"accessToken");
        clearCookies(response,"refreshToken");

        log.info("Cookies cleared successfully");
    }


    private void clearCookies(HttpServletResponse response,String name){
        Cookie cookie = new Cookie(name,null);

        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }


    public AuthResponse getProfile(){
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()){
            throw new BadRequestException("User not authenticated");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new ResourceNotFoundException("user not found"));

        return  new AuthResponse(
                "Profile fetch successfully",
                user.getEmail(),
                user.getRole().name()
        );

    }

    public String forgotPassword(String email){
        log.info("Forgot password request for email: {}",email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found"));

        redisOtpService.checkResendThrottle(email);

        String otp = otpUtil.generateOtp();

        redisOtpService.saveOtp("reset: "+ email, otp);

        emailService.sendOtp(email,otp);

        return "Password reset OTP set to your email";
    }


    public String verifyResendOtp(String email, String otp){

        log.info("Password reset OTP verification for email: {}", email);

        String storedOtp = redisOtpService.getOtp("reset: "+email);

        if (storedOtp == null){
            throw new OtpException("OTP expired");
        }

        if(!storedOtp.equals(otp)){
            throw new OtpException("Invalid OTP");
        }

        return "Otp Verified successfully";


    }

    public String resetPassword(ResetPasswordRequest request){
        log.info("Reset password request for email: {}", request.getEmail());
        String storedOtp = redisOtpService.getOtp("reset:"+ request.getEmail());

        if(storedOtp == null){
            throw new OtpException("OTP expired");
        }

        if(!storedOtp.equals(request.getOtp())){
            throw new OtpException("Invalid OTP");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        redisOtpService.deleteOtp("reset:"+request.getEmail());

        log.info("Password reset successfully for {}",request.getEmail());

        return "Password updated successfully";
    }



}
