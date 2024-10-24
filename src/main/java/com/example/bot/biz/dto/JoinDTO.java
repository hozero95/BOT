package com.example.bot.biz.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 회원 가입 DTO
 */
@SuppressWarnings("SpellCheckingInspection")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JoinDTO {
    @NotNull
    private String usernm;

    @NotNull
    private String birthdate;
}
