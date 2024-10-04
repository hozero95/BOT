package com.example.bot.biz.repository;

import com.example.bot.biz.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Refresh Repository
 */
@Repository
public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {
    Boolean existsByToken(String token);

    @Transactional
    void deleteByToken(String token);
}
