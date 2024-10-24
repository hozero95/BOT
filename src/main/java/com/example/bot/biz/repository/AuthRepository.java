package com.example.bot.biz.repository;

import com.example.bot.biz.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Auth Repository
 */
@SuppressWarnings("SpellCheckingInspection")
@Repository
public interface AuthRepository extends JpaRepository<Auth, Long> {
    Auth findByUsercd(String usercd);
}
