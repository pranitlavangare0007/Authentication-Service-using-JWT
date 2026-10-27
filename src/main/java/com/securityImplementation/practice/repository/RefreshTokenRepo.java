package com.securityImplementation.practice.repository;

import com.securityImplementation.practice.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepo extends JpaRepository<RefreshToken,Long> {

    Optional<RefreshToken> findByToken(String token);
    @Modifying
    @Transactional
    @Query("update RefreshToken t set t.revoked = true where t.token = :token and t.revoked = false")
    int revokeIfActive(@Param("token") String token);

    @Modifying
    @Transactional
    @Query("update RefreshToken t set t.revoked = true where t.user.id = :userId and t.revoked = false")
    int revokeAllForUser(@Param("userId") Long userId);
}
