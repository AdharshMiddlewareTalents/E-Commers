package com.ecommers.auth_service.service;

import com.ecommers.auth_service.entity.AccountStatus;
import com.ecommers.auth_service.entity.Role;
import com.ecommers.auth_service.entity.User;
import com.ecommers.auth_service.repsoitory.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void createAdminIfNotExist(){

        System.out.println("🔥 AdminSeeder executed");

        String adminEmail = "admin@ecommers.com";

        if(userRepository.findByEmail(adminEmail).isEmpty()){

            User admin = User.builder()
                    .name("Super Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ROLE_ADMIN)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();

            userRepository.save(admin);

            System.out.println("✅ Default Admin Created");
        }
    }
}
