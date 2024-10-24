package com.example.bot.biz.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Refresh Token Entity
 */
@SuppressWarnings({"JpaDataSourceORMInspection", "SpellCheckingInspection"})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "refresh_token")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long refreshTokenId; // 리프레시토큰 ID

    @Column(name = "usercd")
    private String usercd; // 회원코드

    @Column(name = "token")
    private String token; // 토큰

    @Column(name = "expireddate")
    private LocalDateTime expireddate; // 만료일

    @Column(name = "createdate")
    private LocalDateTime createdate; // 생성일
}
