package com.ecommers.auth_service.service;

import com.ecommers.auth_service.entity.AccountStatus;
import com.ecommers.auth_service.entity.Role;
import com.ecommers.auth_service.entity.User;
import com.ecommers.auth_service.repsoitory.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.email}")
    private String admineEmail;

    @PostConstruct
    public void createAdminIfNotExist(){
        log.info("Admin seeder executed");

        if(userRepository.findByEmail(admineEmail).isEmpty()){

            User admin = User.builder()
                    .name("Super Admin")
                    .email(admineEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ROLE_ADMIN)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();

            userRepository.save(admin);

            log.info("Default Admin Created");
        }
    }
}
