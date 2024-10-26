package com.example.bot.biz.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 회원 가입 DTO
 */
@SuppressWarnings("SpellCheckingInspection")
public class SignupDTO {
    @Data
    public static class Request {
        @NotNull
        private String usernm;

        @NotNull
        private String birthdate;
    }
}
