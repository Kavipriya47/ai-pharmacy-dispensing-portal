package com.pharmacy.dispensing.auth.repository;

import com.pharmacy.dispensing.auth.entity.RefreshToken;
import com.pharmacy.dispensing.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
