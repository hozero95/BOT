package com.example.bot.biz.entity;

import com.example.bot.biz.entity.core.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * User Entity
 */
@SuppressWarnings({"JpaDataSourceORMInspection", "SpellCheckingInspection"})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "m_user")
public final class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;  // 사원ID

    @Column(name = "password")
    private String password; // 비밀번호

    @Column(name = "usercd")
    private String usercd; // 사원코드

    @Column(name = "usernm")
    private String usernm; // 사원명

    @Column(name = "birthdate")
    private LocalDateTime birthdate; // 생일일자

    @Column(name = "joindate")
    private LocalDateTime joindate; // 입사일자
}