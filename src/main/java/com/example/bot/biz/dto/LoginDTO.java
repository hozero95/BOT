package com.example.bot.biz.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 로그인 DTO
 */
@SuppressWarnings("SpellCheckingInspection")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginDTO {
    @NotNull
    private String usercd;

    @NotNull
    private String password;
}
