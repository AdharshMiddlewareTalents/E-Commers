package com.ecommers.auth_service.repsoitory;

import com.ecommers.auth_service.entity.RefreshToken;

import com.ecommers.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

}
