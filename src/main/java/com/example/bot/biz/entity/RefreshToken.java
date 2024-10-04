package com.example.bot.biz.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Refresh Token Entity
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "t_refresh_token")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long refreshTokenId; // 리프레시토큰 ID

    @Column(name = "email")
    private String email; // 이메일

    @Column(name = "token")
    private String token; // 토큰

    @Column(name = "expired_dt")
    private LocalDateTime expiredDt; // 만료일

    @Column(name = "create_dt")
    private LocalDateTime createDt; // 생성일
}
