package com.example.bot.biz.entity;

import com.example.bot.biz.entity.core.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Auth Entity
 */
@SuppressWarnings({"JpaDataSourceORMInspection", "SpellCheckingInspection"})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "M_AUTH")
public class Auth extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "authid")
    private Long authid;  // 권한ID

    @Column(name = "usercd")
    private String usercd;  // 사원코드

    @Column(name = "auth")
    private String auth; // 권한
}
